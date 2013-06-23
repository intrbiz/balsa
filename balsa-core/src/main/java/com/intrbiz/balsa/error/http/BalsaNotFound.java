package com.intrbiz.balsa.error.http;

import com.intrbiz.balsa.error.BalsaHTTPError;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;


/**
 * No route was found to handle the current request
 */
public class BalsaNotFound extends BalsaHTTPError
{
    public static final HTTPStatus STATUS = HTTPStatus.NotFound;
    
    private static final long serialVersionUID = 1L;

    public BalsaNotFound(String message, Throwable cause)
    {
        super(STATUS, message, cause);
    }
    
    public BalsaNotFound(Throwable cause)
    {
        super(STATUS, cause);
    }

    public BalsaNotFound(String message)
    {
        super(STATUS, message);
    }
    
    public BalsaNotFound()
    {
        super(STATUS);
    }
}
