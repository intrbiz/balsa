package com.intrbiz.balsa.engine.security.info;

import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;

/**
 * Marker interface for authentication information
 */
public interface AuthenticationInfo
{
    String primaryAuthenticationMethodName();
    
    Object primaryAuthenticationMethodDetail();
    
    String secondaryAuthenticationMethodName();
    
    Object secondaryAuthenticationMethodDetail();
    
    default boolean isTwoFactorAuthenticated()
    {
        return this.primaryAuthenticationMethodName() != null && this.secondaryAuthenticationMethodName() != null;
    }
    
    default boolean isBackupCodeUsed()
    {
        return AuthenticationMethod.BACKUP_CODE.equals(this.secondaryAuthenticationMethodName());
    }
}
