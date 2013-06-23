package com.intrbiz.balsa.error;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;

public class BalsaHTTPError extends BalsaException
{
    private static final long serialVersionUID = 1L;
    
    private final HTTPStatus status;
    
    public BalsaHTTPError(HTTPStatus status)
    {
        super();
        this.status = status;
    }
    
    public BalsaHTTPError(HTTPStatus status, String message)
    {
        super(message);
        this.status = status;
    }
    
    public BalsaHTTPError(HTTPStatus status, Throwable cause)
    {
        super(cause);
        this.status = status;
    }
    
    public BalsaHTTPError(HTTPStatus status, String message, Throwable cause)
    {
        super(message, cause);
        this.status = status;
    }
    
    public final HTTPStatus getStatus()
    {
        return this.status;
    }
    
    public final int getStatusCode()
    {
        return this.status.getCode();
    }
    
    public final String getStatusMessage()
    {
        return this.status.getMessage();
    }
}
