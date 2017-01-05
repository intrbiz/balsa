package com.intrbiz.balsa.engine.security;

import java.security.Principal;

import com.intrbiz.balsa.error.BalsaSecurityException;

/**
 * The extensions needed for a Backup Code security engine
 */
public interface BackupCodeSecurityEngine
{
    /**
     * Verify that the backup code for the given principal, throwing a BalsaSecurityException if the code is not valid
     * @param principal the principal to verify the backup code against
     * @param backupCode the backup code
     * @throws BalsaSecurityException should the backup code not be valid or if anything else goes wrong
     */
    void verifyBackupCode(Principal principal, String backupCode) throws BalsaSecurityException;
    
    /**
     * Update the backup code as used
     * @param principal the principal
     * @param backupCode the backup code which was used to authenticate the principal
     * @throws BalsaSecurityException should anything go wrong
     */
    void updateBackupCode(Principal principal, String backupCode) throws BalsaSecurityException;
}
