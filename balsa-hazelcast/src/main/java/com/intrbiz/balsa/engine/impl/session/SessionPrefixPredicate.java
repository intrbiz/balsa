package com.intrbiz.balsa.engine.impl.session;

import java.util.Map.Entry;

import com.hazelcast.query.Predicate;

public final class SessionPrefixPredicate implements Predicate<String, Object>
{
    private static final long serialVersionUID = 1L;
    
    private String sessionId;
    
    public SessionPrefixPredicate()
    {
        super();
    }
    
    public SessionPrefixPredicate(String sessionId)
    {
        super();
        this.sessionId = sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    @Override
    public boolean apply(Entry<String, Object> mapEntry)
    {
        return mapEntry.getKey().startsWith(this.getSessionId());
    }
}
