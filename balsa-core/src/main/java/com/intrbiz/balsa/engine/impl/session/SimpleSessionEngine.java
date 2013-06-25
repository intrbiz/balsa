package com.intrbiz.balsa.engine.impl.session;

import java.security.SecureRandom;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.listener.BalsaListener;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class SimpleSessionEngine extends AbstractBalsaEngine implements SessionEngine, Runnable
{
    private int poolSize = BalsaListener.DEFAULT_POOL_SIZE;

    private int sessionLifetime = 30; // 30 mins

    private ConcurrentMap<String, SimpleSession> sessions;

    private volatile boolean run = false;

    private Thread thread;

    private Logger logger = Logger.getLogger(SimpleSessionEngine.class);
    
    private AtomicLong idCounter = new AtomicLong();
    
    private volatile long idCounterMask = 0xba15a;
    
    private long idCounterMaskLastChanged = System.currentTimeMillis();
    
    private SecureRandom idRandom = new SecureRandom();
    
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

    public void setPoolSize(int poolSize)
    {
        this.poolSize = poolSize;
    }

    public int getPoolSize()
    {
        return this.poolSize;
    }

    public int getSessionLifetime()
    {
        return sessionLifetime;
    }

    public void setSessionLifetime(int sessionLifetime)
    {
        this.sessionLifetime = sessionLifetime;
    }

    @Override
    public String makeId()
    {
        return "ba15a" + Long.toHexString(this.idRandom.nextLong()) + Long.toHexString(this.idCounter.incrementAndGet() ^ this.idCounterMask) + Long.toHexString(this.idRandom.nextLong());
    }
    
    private byte[] makeRequestToken()
    {
        byte[] token = new byte[32];
        this.idRandom.nextBytes(token);
        return token;
    }
    
    protected BalsaSession newSession(String sessionId)
    {
        if (logger.isTraceEnabled()) logger.trace("Creating session: " + sessionId);
        //
        this.activeSessions.inc();
        this.createdSessions.mark();
        TimerContext timer = this.sessionLifeTimer.time();
        //
        SimpleSession session = new SimpleSession(this.application, sessionId, this.poolSize, timer);
        session.setRequestToken(this.makeRequestToken());
        //
        this.sessions.put(sessionId, session);
        //
        return session;
    }

    @Override
    public BalsaSession getSession(String sessionId)
    {
        BalsaSession session = this.sessions.get(sessionId);
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
        long evictTime = System.currentTimeMillis() - (this.sessionLifetime * 60 * 1000);
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
            // init the id mask to a random
            this.idCounterMask = this.idRandom.nextLong();
            logger.trace("Session mask set to: " + this.idCounterMask);
            this.idCounterMaskLastChanged = System.currentTimeMillis();
        }
    }

    @Override
    public void start() throws BalsaException
    {
        this.sessions = new ConcurrentHashMap<String, SimpleSession>(poolSize * 100, 0.75F, poolSize);
        logger.info("Configured with session lifetime of " + this.sessionLifetime + " minutes");
        // init the id mask to a random
        this.idCounterMask = this.idRandom.nextLong();
        logger.trace("Session mask set to: " + this.idCounterMask);
        this.idCounterMaskLastChanged = System.currentTimeMillis();
        //
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