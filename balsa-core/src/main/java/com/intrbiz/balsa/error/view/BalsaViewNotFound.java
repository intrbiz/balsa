package com.intrbiz.balsa.error.view;

import com.intrbiz.balsa.error.http.BalsaNotFound;


/**
 * The requested view could not be found
 */
public class BalsaViewNotFound extends BalsaNotFound
{   
    private static final long serialVersionUID = 1L;

    public BalsaViewNotFound(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public BalsaViewNotFound(Throwable cause)
    {
        super(cause);
    }

    public BalsaViewNotFound(String message)
    {
        super(message);
    }
    
    public BalsaViewNotFound()
    {
        super();
    }
}
