package com.intrbiz.balsa.engine.impl.session;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;

public abstract class AbstractSessionEngine extends AbstractBalsaEngine implements SessionEngine
{
    private Logger logger = Logger.getLogger(AbstractSessionEngine.class);
    
    private AtomicLong idCounter = new AtomicLong();
    
    private volatile long idCounterMask = 0xba15a;
    
    private SecureRandom idRandom = new SecureRandom();
    
    private int sessionLifetime = 30; // 30 mins

    public AbstractSessionEngine()
    {
        super();
        this.changeSessionMask();
    }

    public final int getSessionLifetime()
    {
        return sessionLifetime;
    }

    public final void setSessionLifetime(int sessionLifetime)
    {
        this.sessionLifetime = sessionLifetime;
    }

    @Override
    public final String makeId()
    {
        return "ba15a" + Long.toHexString(this.idRandom.nextLong()) + Long.toHexString(this.idCounter.incrementAndGet() ^ this.idCounterMask) + Long.toHexString(this.idRandom.nextLong());
    }
    
    protected void changeSessionMask()
    {
        // init the id mask to a random
        this.idCounterMask = this.idRandom.nextLong();
        logger.trace("Session mask set to: " + this.idCounterMask);
    }
}
