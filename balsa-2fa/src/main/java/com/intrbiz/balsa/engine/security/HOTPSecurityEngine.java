package com.intrbiz.balsa.engine.security;

import java.security.Principal;
import java.util.List;

import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.util.HOTP.HOTPState;
import com.intrbiz.util.HOTPRegistration;

/**
 * The extensions needed for a HOTP security engine
 */
public interface HOTPSecurityEngine
{
    /**
     * Get the list HOTP registrations for the given principal
     * @param principal the principal
     * @return the list of HOTP registrations
     * @throws BalsaSecurityException should anything go wrong
     */
    List<HOTPRegistration> getHOTPRegistrationsForPrincipal(Principal principal) throws BalsaSecurityException;
    
    /**
     * Update the HOTP registration with the updated state
     * @param principal the principal
     * @param registration the HOTP registration with the updated state
     * @param the next HOTP state which the registration should be updated with
     * @throws BalsaSecurityException should anything go wrong
     */
    void updateHOTPRegistration(Principal principal, HOTPRegistration registration, HOTPState nextState) throws BalsaSecurityException;
    
    /**
     * Apply any application specific validations to the HOTP registration which the principal authenticated with, 
     * this MUST throw a BalsaSecurityException should the validation fail.
     * @param principal the principal
     * @param registration the HOTP registration which was used to authenticate the principal
     * @throws BalsaSecurityException should anything go wrong
     */
    void validateHOTPRegistration(Principal principal, HOTPRegistration registration) throws BalsaSecurityException;
}
