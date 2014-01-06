package com.intrbiz.balsa.error;

import com.intrbiz.balsa.BalsaException;

/**
 * A error was thrown while validating request values
 */
public class BalsaValidationError extends BalsaException
{
    private static final long serialVersionUID = 1L;

    public BalsaValidationError()
    {
        super();
    }

    public BalsaValidationError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaValidationError(String message)
    {
        super(message);
    }

    public BalsaValidationError(Throwable cause)
    {
        super(cause);
    }
}
