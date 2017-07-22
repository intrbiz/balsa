package com.intrbiz.balsa.error.security;

import com.intrbiz.balsa.error.BalsaSecurityException;

/**
 * A principal has been locked out of the system
 */
public class BalsaPrincipalLockout extends BalsaSecurityException
{
    private static final long serialVersionUID = 1L;

    public BalsaPrincipalLockout()
    {
        super();
    }

    public BalsaPrincipalLockout(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaPrincipalLockout(String message)
    {
        super(message);
    }

    public BalsaPrincipalLockout(Throwable cause)
    {
        super(cause);
    }

}
