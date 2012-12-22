package com.intrbiz.balsa;


public class BalsaException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public BalsaException()
    {
        super();
    }

    public BalsaException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BalsaException(String message)
    {
        super(message);
    }

    public BalsaException(Throwable cause)
    {
        super(cause);
    }
    
}
