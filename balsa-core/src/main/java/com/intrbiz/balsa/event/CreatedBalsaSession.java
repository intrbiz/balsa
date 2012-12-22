package com.intrbiz.balsa.event;

import com.intrbiz.balsa.engine.session.BalsaSession;

public class CreatedBalsaSession extends BalsaSessionEvent
{
    public CreatedBalsaSession(BalsaSession session)
    {
        super(session);
    }
}
