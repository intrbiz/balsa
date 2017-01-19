package com.intrbiz.balsa.engine.impl.security.method;

import java.security.Principal;
import java.util.List;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.HOTPSecurityEngine;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.HOTPCredentials;
import com.intrbiz.balsa.engine.security.info.HOTPAuthenticationDetail;
import com.intrbiz.balsa.engine.security.method.AuthenticatedPrincipal;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.util.CounterHOTP;
import com.intrbiz.util.CounterHOTP.CounterHOTPState;
import com.intrbiz.util.HOTP.VerificationResult;
import com.intrbiz.util.HOTPRegistration;

public class HOTPAuthenticationMethod extends BaseAuthenticationMethod<HOTPCredentials>
{
    protected final CounterHOTP hotp = new CounterHOTP();
    
    protected HOTPSecurityEngine securityEngine;
    
    public HOTPAuthenticationMethod()
    {
        super(HOTPCredentials.class, AuthenticationMethod.HOTP);
    }
    
    @Override
    public void setup(SecurityEngine engine) throws BalsaException
    {
        if (! (engine instanceof HOTPSecurityEngine))
            throw new BalsaException("The HOTP authentication method can only be registered against a HOTPSecurityEngine");
        this.securityEngine = (HOTPSecurityEngine) engine;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthenticationState state, HOTPCredentials credentials) throws BalsaSecurityException
    {
        Principal principal = state.authenticatingPrincipal();
        if (principal == null) throw new BalsaSecurityException("No principal is currently in the process of authenticating, HOTP authentication cannot proceed");
        // get the HOTP registration list for the principal
        List<HOTPRegistration> hotps = this.securityEngine.getHOTPRegistrationsForPrincipal(principal);
        if (hotps == null || hotps.isEmpty()) throw new BalsaSecurityException("The principal does not have any HOTP registrations");
        // attempt each HOTP registration
        for (HOTPRegistration hotp : hotps)
        {
            // verify the HOTP code
            VerificationResult<CounterHOTPState> result = this.hotp.verifyOTP(hotp.getHOTPSecret(), hotp.getHOTPState(), credentials.getAuthenticationCode());
            if (result.isValid())
            {
                this.securityEngine.validateHOTPRegistration(principal, hotp);
                // ensure we update the registration counter
                this.securityEngine.updateHOTPRegistration(principal, hotp, result.getNextState());
                // successfully authenticated using the HOTP registration
                return new AuthenticatedPrincipal(principal, this.name, this.createAuthenticationInfoDetail(principal, hotp));
            }
        }
        throw new BalsaSecurityException("Failed to authenticate principal using any HOTP registration");
    }

    @Override
    public AuthenticationChallenge generateAuthenticationChallenge(Principal principal) throws BalsaSecurityException
    {
        return null;
    }
    
    protected Object createAuthenticationInfoDetail(Principal principal, HOTPRegistration hotp) throws BalsaSecurityException
    {
        return new HOTPAuthenticationDetail(hotp);
    }
    
    public CounterHOTP getHOTP()
    {
        return this.hotp;
    }
}
