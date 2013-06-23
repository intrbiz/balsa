package com.intrbiz.balsa.error;

import com.intrbiz.balsa.BalsaException;

/**
 * Some kind of IO error happened while processing the request
 */
public class BalsaIOError extends BalsaException
{
    private static final long serialVersionUID = 1L;

    public BalsaIOError()
    {
        super();
    }

    public BalsaIOError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaIOError(String message)
    {
        super(message);
    }

    public BalsaIOError(Throwable cause)
    {
        super(cause);
    }
}
