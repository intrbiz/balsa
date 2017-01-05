package com.intrbiz.balsa.engine.security;

import java.security.Principal;
import java.util.List;

import com.intrbiz.balsa.error.BalsaSecurityException;
import com.yubico.u2f.data.DeviceRegistration;

/**
 * The extensions needed for a U2F security engine
 */
public interface U2FSecurityEngine
{
    /**
     * Get the list of U2F device registrations for the given principal
     * @param principal the principal
     * @return the list of U2F device registrations
     * @throws BalsaSecurityException should anything go wrong
     */
    List<DeviceRegistration> getDeviceRegistrationsForPrincipal(Principal principal) throws BalsaSecurityException;
    
    /**
     * Get the U2F appId for the given principal
     * @param principal the principal
     * @return the appId
     * @throws BalsaSecurityException should anything go wrong
     */
    String getAppIdForPrincipal(Principal principal) throws BalsaSecurityException;
    
    /**
     * Apply any application specific validations to the U2F device which the principal authenticated with, 
     * this MUST throw a BalsaSecurityException should the validation fail.
     * @param principal the principal
     * @param u2fDevice the U2F device registration which was used for authentication
     * @throws BalsaSecurityException should anything go wrong
     */
    void validateDeviceRegistration(Principal principal, DeviceRegistration u2fDevice) throws BalsaSecurityException;
    
    /**
     * Update the U2F device registration with the updated counter value
     * @param principal the principal
     * @param u2fDevice the U2F device registration which was used for authentication
     * @throws BalsaSecurityException should anything go wrong
     */
    void updateDeviceRegistration(Principal principal, DeviceRegistration u2fDevice) throws BalsaSecurityException;
}