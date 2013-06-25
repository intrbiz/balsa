package com.intrbiz.balsa.error.security;

import com.intrbiz.balsa.error.BalsaSecurityException;

public class BalsaInvalidRequest extends BalsaSecurityException
{
    private static final long serialVersionUID = 1L;

    public BalsaInvalidRequest()
    {
        super();
    }

    public BalsaInvalidRequest(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaInvalidRequest(String message)
    {
        super(message);
    }

    public BalsaInvalidRequest(Throwable cause)
    {
        super(cause);
    }

}
