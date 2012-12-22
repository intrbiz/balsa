package com.intrbiz.balsa.listener.middleware;

import static com.intrbiz.Util.isEmpty;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;

public class SessionMiddleware extends AbstractMiddleware
{
    public static final String COOKIE_NAME = "BalsaSession";
    
    private Logger logger = Logger.getLogger(SessionMiddleware.class);

    @Override
    public boolean before(BalsaContext context) throws IOException
    {
        // Get the session id
        String sessionId = context.getRequest().cookie(COOKIE_NAME);
        if (isEmpty(sessionId))
        {
            sessionId = context.getApplication().getSessionEngine().makeId();
            if (logger.isTraceEnabled()) logger.trace("Setting cookie for session: " + sessionId);
            context.getResponse().header("Set-Cookie", COOKIE_NAME + "=" + sessionId + "; Path=/");
        }
        // Load the session
        context.setSession(context.getApplication().getSessionEngine().getSession(sessionId));
        return true;
    }

    @Override
    public void after(BalsaContext context) throws IOException
    {
    }

}
