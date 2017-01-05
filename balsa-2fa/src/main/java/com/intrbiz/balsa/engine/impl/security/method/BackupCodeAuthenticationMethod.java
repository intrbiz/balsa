package com.intrbiz.balsa.engine.impl.security.method;

import java.security.Principal;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SecurityEngine;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.BackupCodeSecurityEngine;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.BackupCodeCredentials;
import com.intrbiz.balsa.engine.security.info.BackupCodeAuthenticationDetail;
import com.intrbiz.balsa.engine.security.method.AuthenticatedPrincipal;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;

public class BackupCodeAuthenticationMethod extends BaseAuthenticationMethod<BackupCodeCredentials>
{    
    protected BackupCodeSecurityEngine securityEngine;
    
    public BackupCodeAuthenticationMethod()
    {
        super(BackupCodeCredentials.class, AuthenticationMethod.NAMES.BACKUP_CODE);
    }
    
    @Override
    public void setup(SecurityEngine engine) throws BalsaException
    {
        if (! (engine instanceof BackupCodeSecurityEngine))
            throw new BalsaException("The HOTP authentication method can only be registered against a BackupCodeSecurityEngine");
        this.securityEngine = (BackupCodeSecurityEngine) engine;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthenticationState state, BackupCodeCredentials credentials) throws BalsaSecurityException
    {
        // get the principal
        Principal principal = state.authenticatingPrincipal();
        if (principal == null) throw new BalsaSecurityException("No principal is currently in the process of authenticating, HOTP authentication cannot proceed");
        // get the code
        String code = credentials.getBackupCode();
        if (Util.isEmpty(code)) throw new BalsaSecurityException("Invalid backup code given");
        // verify the backup code
        this.securityEngine.verifyBackupCode(principal, code);
        // mark the backup code as used
        this.securityEngine.updateBackupCode(principal, code);
        // all done
        return new AuthenticatedPrincipal(principal, this.name, this.createAuthenticationInfoDetail(principal, code));
    }
    
    @Override
    public void verify(AuthenticationState state, BackupCodeCredentials credentials) throws BalsaSecurityException
    {
        throw new BalsaSecurityException("Backup codes cannot be used to verify a principal");
    }

    @Override
    public AuthenticationChallenge generateAuthenticationChallenge(Principal principal) throws BalsaSecurityException
    {
        return null;
    }
    
    protected Object createAuthenticationInfoDetail(Principal principal, String backupCode) throws BalsaSecurityException
    {
        return new BackupCodeAuthenticationDetail(backupCode);
    }
}
