package com.intrbiz.balsa.metrics.collector;

import com.intrbiz.Balsa;
import com.intrbiz.balsa.event.CreatedBalsaSession;
import com.intrbiz.balsa.event.DestroyedBalsaSession;
import com.intrbiz.balsa.event.SimpleBalsaSessionListener;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricsRegistry;

public class SessionMetricsCollector extends SimpleBalsaSessionListener
{
    private final Counter sessions;
    
    public SessionMetricsCollector(MetricsRegistry registry)
    {
        this.sessions = registry.newCounter(Balsa.class, "active-sessions");
    }

    @Override
    public void createdBalsaSession(CreatedBalsaSession event)
    {
        this.sessions.inc();
    }

    @Override
    public void destroyedBalsaSession(DestroyedBalsaSession event)
    {
        this.sessions.dec();
    }
}
