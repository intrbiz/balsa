package com.intrbiz.balsa.listener.middleware;

import static com.intrbiz.Util.isEmpty;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.util.HTTPUtil;

public class CookieMiddleware extends AbstractMiddleware
{
    public static final String COOKIES_HEADER = "Cookie";
    
    @Override
    public boolean before(BalsaContext context) throws IOException
    {
        BalsaRequest request = context.getRequest();
        String cookies = request.getHeader(COOKIES_HEADER);
        if (! isEmpty(cookies)) HTTPUtil.parseCookies(cookies, request);
        return true;
    }

    @Override
    public void after(BalsaContext context) throws IOException
    {
    }

}
