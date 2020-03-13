package com.intrbiz.balsa.engine.impl.security.method;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.U2FSecurityEngine;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.challenge.U2FAuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.U2FAuthenticationChallengeResponse;
import com.intrbiz.balsa.engine.security.info.U2FAuthenticationDetail;
import com.intrbiz.balsa.engine.security.method.AuthenticatedPrincipal;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.yubico.u2f.U2F;
import com.yubico.u2f.attestation.MetadataService;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.SignRequestData;
import com.yubico.u2f.exceptions.NoEligibleDevicesException;
import com.yubico.u2f.exceptions.U2fAuthenticationException;
import com.yubico.u2f.exceptions.U2fBadConfigurationException;

/**
 * A U2F authentication method which needs to be registered against a SecurityEngine which implements U2FSecurityEngine
 */
public class U2FAuthenticationMethod extends BaseAuthenticationMethod<U2FAuthenticationChallengeResponse>
{
    protected final U2F u2f = new U2F();
    
    protected final MetadataService u2fMetadata = new MetadataService();
    
    protected U2FSecurityEngine securityEngine;
    
    public U2FAuthenticationMethod()
    {
        super(U2FAuthenticationChallengeResponse.class, AuthenticationMethod.U2F);
    }
    
    @Override
    public void setup(SecurityEngine engine) throws BalsaException
    {
        if (! (engine instanceof U2FSecurityEngine))
            throw new BalsaException("The U2F authentication method can only be registered against a U2FSecurityEngine");
        this.securityEngine = (U2FSecurityEngine) engine;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthenticationState state, U2FAuthenticationChallengeResponse credentials) throws BalsaSecurityException
    {
        Principal principal = state.authenticatingPrincipal();
        if (principal == null) throw new BalsaSecurityException("No principal is currently in the process of authenticating, U2F authentication cannot proceed");
        // get the U2F device list for the principal
        List<DeviceRegistration> devices = this.securityEngine.getDeviceRegistrationsForPrincipal(principal);
        if (devices == null || devices.isEmpty()) throw new BalsaSecurityException("The principal does not have any U2F devices");
        // get the authentication challenge
        SignRequestData challenge = credentials.getChallenge();
        // fall back to the challenge in the state
        if (challenge == null)
        {
            Map<String, AuthenticationChallenge> challenges = state.challenges();
            if (challenges != null)
            {
                AuthenticationChallenge storedChallenge = challenges.get(this.name);
                if (storedChallenge instanceof U2FAuthenticationChallenge)
                {
                    challenge = ((U2FAuthenticationChallenge) storedChallenge).getChallenge();
                }
            }
        }
        // we need the challenge
        if (challenge == null) throw new BalsaSecurityException("Could not finish U2F authentication, no challenge provided");
        // process the U2F authentication
        try
        {
            // execute the U2f authentication
            DeviceRegistration device = this.u2f.finishSignature(challenge, credentials.getResponse(), devices);
            // apply any additional validations to the U2F device
            this.securityEngine.validateDeviceRegistration(principal, device);
            // update the device registration with the new counter value
            this.securityEngine.updateDeviceRegistration(principal, device);
            // all done
            return new AuthenticatedPrincipal(principal, this.name, this.createAuthenticationInfoDetail(principal, device));
        }
        catch (U2fAuthenticationException e)
        {
            throw new BalsaSecurityException("U2F authentication failed", e);
        }
        
    }

    @Override
    public AuthenticationChallenge generateAuthenticationChallenge(Principal principal) throws BalsaSecurityException
    {
        // get the U2F device list for the principal
        List<DeviceRegistration> devices = this.securityEngine.getDeviceRegistrationsForPrincipal(principal);
        if (devices != null && devices.size() > 0)
        {
            // get the U2F appId for this principal
            String appId = this.securityEngine.getAppIdForPrincipal(principal);
            if (Util.isEmpty(appId)) throw new BalsaSecurityException("Could not get U2F appId for principal");
            // start the U2F login
            try
            {
                return new U2FAuthenticationChallenge(this.u2f.startSignature(appId, devices));
            }
            catch (NoEligibleDevicesException | U2fBadConfigurationException e)
            {
                throw new BalsaSecurityException("Failed to create U2F challenge", e);
            }
        }
        return null;
    }
    
    protected Object createAuthenticationInfoDetail(Principal principal, DeviceRegistration u2fDevice) throws BalsaSecurityException
    {
        return new U2FAuthenticationDetail(u2fDevice);
    }
    
    public U2F getU2F()
    {
        return this.u2f;
    }
    
    public MetadataService getU2FMetadataService()
    {
        return this.u2fMetadata;
    }
}
