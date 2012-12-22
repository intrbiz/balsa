package com.intrbiz.balsa.error;

import com.intrbiz.balsa.BalsaException;

/**
 * A framework internal error has happened.
 * 
 * Not to be used by applications!
 */
public class BalsaInternalError extends BalsaException
{
    private static final long serialVersionUID = 1L;

    public BalsaInternalError()
    {
        super();
    }

    public BalsaInternalError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaInternalError(String message)
    {
        super(message);
    }

    public BalsaInternalError(Throwable cause)
    {
        super(cause);
    }
    
}
