package com.intrbiz.balsa.error.http;

import com.intrbiz.balsa.error.BalsaHTTPError;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

public class BalsaInternalServerError extends BalsaHTTPError
{
    public static final HTTPStatus STATUS = HTTPStatus.InternalServerError;
    
    private static final long serialVersionUID = 1L;

    public BalsaInternalServerError(String message, Throwable cause)
    {
        super(STATUS, message, cause);
    }
    
    public BalsaInternalServerError(Throwable cause)
    {
        super(STATUS, cause);
    }

    public BalsaInternalServerError(String message)
    {
        super(STATUS, message);
    }
    
    public BalsaInternalServerError()
    {
        super(STATUS);
    }
}
