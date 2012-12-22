package com.intrbiz.balsa.listener.middleware;

import java.io.IOException;
import java.nio.charset.Charset;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.util.HTTPUtil;

public class QueryStringMiddleware extends AbstractMiddleware
{
    private static final Charset SCGI_CHARSET = Charset.forName("ISO-8859-1");
    
    public static final String WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    
    @Override
    public boolean before(BalsaContext context) throws IOException
    {
        BalsaRequest request = context.getRequest();
        // parse the query string
        HTTPUtil.parseQueryString(request.getQueryString(), request);
        // parse a posted query string
        if (WWW_FORM_URLENCODED.equals(request.getContentType()))
        {
            // buffer the request body in
            byte[] body = this.readBodyBytes(request);
            // parse the parameters
            String postParameters = new String(body, SCGI_CHARSET);
            HTTPUtil.parseQueryString(postParameters, request);
        }
        // continue processing the request
        return true;
    }
    
}
