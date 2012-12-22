package com.intrbiz.balsa.event;

import com.intrbiz.balsa.engine.session.BalsaSession;

public abstract class BalsaSessionEvent implements BalsaEvent
{
    private final BalsaSession session;
    
    public BalsaSessionEvent(BalsaSession session)
    {
        this.session = session;
    }
    
    public BalsaSession getSession()
    {
        return this.session;
    }
}
