package com.intrbiz.balsa.engine;

import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.event.BalsaEventAnnouncer;
import com.intrbiz.balsa.event.BalsaSessionEvent;

public interface SessionEngine extends BalsaEngine, BalsaEventAnnouncer<BalsaSessionEvent>
{
    public static final int DEFAULT_SESSION_LIFETIME = 30;
    
    int getPoolSize();
    
    void setPoolSize(int poolSize);
    
    int getSessionLifetime();

    void setSessionLifetime(int sessionLifetime);
    
    String makeId();
    
    BalsaSession getSession(String sessionId);
}
