package com.intrbiz.balsa.engine;

import com.intrbiz.balsa.engine.session.BalsaSession;

public interface SessionEngine extends BalsaEngine
{
    public static final int DEFAULT_SESSION_LIFETIME = 30;
    
    int getSessionLifetime();

    void setSessionLifetime(int sessionLifetime);
    
    String makeId();
    
    BalsaSession getSession(String sessionId);
}
