package com.intrbiz.balsa.engine.impl.session;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

public class SimpleSessionEngine extends AbstractSessionEngine implements Runnable
{
    private ConcurrentMap<String, SimpleSession> sessions;

    private volatile boolean run = false;

    private Thread thread;
    
    private long idCounterMaskLastChanged = System.currentTimeMillis();

    private Logger logger = Logger.getLogger(SimpleSessionEngine.class);
    
    /* Metrics */
    
    private final Counter activeSessions;
    
    private final Meter createdSessions;
    
    private final Meter destroyedSessions;
    
    private final Timer sessionLifeTimer;

    public SimpleSessionEngine()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.balsa");
        this.activeSessions    = source.getRegistry().counter(Witchcraft.name(SessionEngine.class, "active-sessions"));
        this.createdSessions   = source.getRegistry().meter(Witchcraft.name(SessionEngine.class, "created-sessions"));
        this.destroyedSessions = source.getRegistry().meter(Witchcraft.name(SessionEngine.class, "destroyed-sessions"));
        this.sessionLifeTimer  = source.getRegistry().timer(Witchcraft.name(SessionEngine.class, "session-lifetime"));
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
        Timer.Context timer = this.sessionLifeTimer.time();
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