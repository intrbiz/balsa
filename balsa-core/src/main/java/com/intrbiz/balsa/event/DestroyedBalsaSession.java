package com.intrbiz.balsa.event;

import com.intrbiz.balsa.engine.session.BalsaSession;

public class DestroyedBalsaSession extends BalsaSessionEvent
{
    public DestroyedBalsaSession(BalsaSession session)
    {
        super(session);
    }
}
