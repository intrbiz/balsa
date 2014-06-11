package com.intrbiz.balsa.engine.impl.session;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SessionEngine;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class SimpleSessionEngine extends AbstractSessionEngine implements Runnable
{
    private ConcurrentMap<String, SimpleSession> sessions;

    private volatile boolean run = false;

    private Thread thread;
    
    private long idCounterMaskLastChanged = System.currentTimeMillis();

    private Logger logger = Logger.getLogger(SimpleSessionEngine.class);
    
    /* Metrics */
    
    private final Counter activeSessions = Metrics.newCounter(SessionEngine.class, "active-sessions");
    
    private final Meter createdSessions = Metrics.newMeter(SessionEngine.class, "created-sessions", "sessions", TimeUnit.MINUTES);
    
    private final Meter destroyedSessions = Metrics.newMeter(SessionEngine.class, "destroyed-sessions", "sessions", TimeUnit.MINUTES);
    
    private final Timer sessionLifeTimer = Metrics.newTimer(SessionEngine.class, "session-lifetime", TimeUnit.MINUTES, TimeUnit.HOURS);

    public SimpleSessionEngine()
    {
        super();
    }
    
    public String getEngineName()
    {
        return "Balsa-Simple-Session-Engine";
    }
    
    protected SimpleSession newSession(String sessionId)
    {
        if (logger.isTraceEnabled()) logger.trace("Creating session: " + sessionId);
        //
        this.activeSessions.inc();
        this.createdSessions.mark();
        TimerContext timer = this.sessionLifeTimer.time();
        //
        SimpleSession session = new SimpleSession(this.application, sessionId, timer);
        //
        this.sessions.put(sessionId, session);
        //
        return session;
    }
    
    @Override
    public SimpleSession getSession(String sessionId)
    {
        SimpleSession session = this.sessions.get(sessionId);
        if (session == null) session = this.newSession(sessionId);
        session.access();
        return session;
    }

    public void run()
    {
        while (this.run)
        {
            try
            {
                this.sweep();
                synchronized (this)
                {
                    this.wait(10000);
                }
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    protected void sweep()
    {
        long evictTime = System.currentTimeMillis() - (this.getSessionLifetime() * 60 * 1000);
        for (Entry<String, SimpleSession> key : this.sessions.entrySet())
        {
            if (key.getValue().lastAccess() < evictTime)
            {
                if (logger.isTraceEnabled()) logger.trace("Evicting session: " + key.getKey());
                this.sessions.remove(key.getKey());
                key.getValue().deactivate();
                // instrument
                this.activeSessions.dec();
                this.destroyedSessions.mark();
                key.getValue().getTimer().stop();
            }
        }
        // update the session id mask ?
        if ((System.currentTimeMillis() - this.idCounterMaskLastChanged) > (8 * 60 * 60 * 1000))
        {
            this.changeSessionMask();
        }
    }

    @Override
    public void start() throws BalsaException
    {
        this.sessions = new ConcurrentHashMap<String, SimpleSession>(Runtime.getRuntime().availableProcessors() * 100, 0.75F, Runtime.getRuntime().availableProcessors());
        logger.info("Configured with session lifetime of " + this.getSessionLifetime() + " minutes");
        this.run = true;
        this.thread = new Thread(new ThreadGroup("Balsa"), this, "BalsaSessionSweeper");
        this.thread.start();
    }

    @Override
    public void stop()
    {
        this.run = false;
        synchronized (this)
        {
            this.notifyAll();
        }
    }  
}