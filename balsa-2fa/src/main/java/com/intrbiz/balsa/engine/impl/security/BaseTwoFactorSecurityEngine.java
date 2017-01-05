package com.intrbiz.balsa.engine.impl.security;

import com.intrbiz.balsa.engine.impl.security.method.BackupCodeAuthenticationMethod;
import com.intrbiz.balsa.engine.impl.security.method.HOTPAuthenticationMethod;
import com.intrbiz.balsa.engine.impl.security.method.U2FAuthenticationMethod;
import com.intrbiz.balsa.engine.security.BackupCodeSecurityEngine;
import com.intrbiz.balsa.engine.security.HOTPSecurityEngine;
import com.intrbiz.balsa.engine.security.U2FSecurityEngine;

/**
 * A skeleton two factor authentication capable security engine
 */
public abstract class BaseTwoFactorSecurityEngine extends BaseSecurityEngine implements U2FSecurityEngine, HOTPSecurityEngine, BackupCodeSecurityEngine
{
    public BaseTwoFactorSecurityEngine()
    {
        super();
    }

    @Override
    protected void setupDefaultAuthenticationMethods()
    {
        super.setupDefaultAuthenticationMethods();
        // register 2FA authentication methods
        this.registerAuthenticationMethod(new U2FAuthenticationMethod());
        this.registerAuthenticationMethod(new HOTPAuthenticationMethod());
        this.registerAuthenticationMethod(new BackupCodeAuthenticationMethod());
    }
}
