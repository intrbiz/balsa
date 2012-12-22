package com.intrbiz.balsa.error;

import com.intrbiz.balsa.BalsaException;

/**
 * No route was found to handle the current request
 */
public class BalsaNotFound extends BalsaException
{
    private static final long serialVersionUID = 1L;

    public BalsaNotFound()
    {
        super();
    }

    public BalsaNotFound(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaNotFound(String message)
    {
        super(message);
    }

    public BalsaNotFound(Throwable cause)
    {
        super(cause);
    }
    
}
