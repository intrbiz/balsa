package com.intrbiz.balsa.error;

import com.intrbiz.balsa.BalsaException;

public class BalsaSecurityException extends BalsaException
{
    private static final long serialVersionUID = 1L;

    public BalsaSecurityException()
    {
        super();
    }

    public BalsaSecurityException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaSecurityException(String message)
    {
        super(message);
    }

    public BalsaSecurityException(Throwable cause)
    {
        super(cause);
    }
}
