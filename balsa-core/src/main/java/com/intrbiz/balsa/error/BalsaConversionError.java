package com.intrbiz.balsa.error;

import com.intrbiz.balsa.BalsaException;

/**
 * A error was thrown while converting request values
 */
public class BalsaConversionError extends BalsaException
{
    private static final long serialVersionUID = 1L;

    public BalsaConversionError()
    {
        super();
    }

    public BalsaConversionError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaConversionError(String message)
    {
        super(message);
    }

    public BalsaConversionError(Throwable cause)
    {
        super(cause);
    }
}
