package com.intrbiz.balsa.listener.filter;

import static com.intrbiz.Util.isEmpty;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.listener.BalsaFilter;

public class SessionFilter implements BalsaFilter
{   
    @Override
    public void filter(BalsaContext context, BalsaFilterChain next) throws Throwable
    {
        // Get the session id
        String sessionId = context.request().cookie(BalsaSession.COOKIE_NAME);
        if (! isEmpty(sessionId))
        {
            // Load the session
            context.setSession(context.app().getSessionEngine().getSession(sessionId));
        }
        // Invoke the next filter
        next.filter(context);
    }
    
    public String toString()
    {
        return "Session Filter";
    }
}
