package com.intrbiz.balsa.error.http;

import com.intrbiz.balsa.error.BalsaHTTPError;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

public class BalsaBadRequest extends BalsaHTTPError
{
    public static final HTTPStatus STATUS = HTTPStatus.BadRequest;
    
    private static final long serialVersionUID = 1L;

    public BalsaBadRequest(String message, Throwable cause)
    {
        super(STATUS, message, cause);
    }
    
    public BalsaBadRequest(Throwable cause)
    {
        super(STATUS, cause);
    }

    public BalsaBadRequest(String message)
    {
        super(STATUS, message);
    }
    
    public BalsaBadRequest()
    {
        super(STATUS);
    }
}
