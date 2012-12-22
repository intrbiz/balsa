package com.intrbiz.balsa.engine.session.impl;

import java.security.SecureRandom;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.event.BalsaEventDispatcher;
import com.intrbiz.balsa.event.BalsaEventListener;
import com.intrbiz.balsa.event.BalsaEventListenerRegistration;
import com.intrbiz.balsa.event.BalsaSessionEvent;
import com.intrbiz.balsa.event.CreatedBalsaSession;
import com.intrbiz.balsa.event.DestroyedBalsaSession;
import com.intrbiz.balsa.listener.BalsaListener;

public class SimpleSessionEngine extends AbstractBalsaEngine implements SessionEngine, Runnable
{
    private int poolSize = BalsaListener.DEFAULT_POOL_SIZE;

    private int sessionLifetime = 30; // 30 mins

    private ConcurrentMap<String, BalsaSession> sessions;

    private volatile boolean run = false;

    private Thread thread;

    private Logger logger = Logger.getLogger(SimpleSessionEngine.class);
    
    private AtomicLong idCounter = new AtomicLong();
    
    private volatile long idCounterMask = 0xba15a;
    
    private long idCounterMaskLastChanged = System.currentTimeMillis();
    
    private SecureRandom idRandom = new SecureRandom();
    
    private final BalsaEventDispatcher<BalsaSessionEvent> eventDispatcher = new BalsaEventDispatcher<BalsaSessionEvent>();

    public SimpleSessionEngine()
    {
        super();
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

    public String makeId()
    {
        return "ba15a" + Long.toHexString(this.idRandom.nextLong()) + Long.toHexString(this.idCounter.incrementAndGet() ^ this.idCounterMask) + Long.toHexString(this.idRandom.nextLong());
    }

    @Override
    public BalsaSession getSession(String sessionId)
    {
        BalsaSession session = this.sessions.get(sessionId);
        if (session == null)
        {
            if (logger.isTraceEnabled()) logger.trace("Creating session: " + sessionId);
            session = new SimpleSession(this.application, sessionId, this.poolSize);
            this.sessions.put(sessionId, session);
            // send an event
            if (this.eventDispatcher.isListening()) this.eventDispatcher.announce(new CreatedBalsaSession(session));
        }
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
        for (Entry<String, BalsaSession> key : this.sessions.entrySet())
        {
            if (key.getValue().lastAccess() < evictTime)
            {
                if (logger.isTraceEnabled()) logger.trace("Evicting session: " + key.getKey());
                this.sessions.remove(key.getKey());
                key.getValue().deactivate();
                // send event
                if (this.eventDispatcher.isListening()) this.eventDispatcher.announce(new DestroyedBalsaSession(key.getValue()));
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
        this.sessions = new ConcurrentHashMap<String, BalsaSession>(poolSize * 100, 0.75F, poolSize);
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

    @Override
    public BalsaEventListenerRegistration listen(BalsaEventListener<BalsaSessionEvent> listener)
    {
        return this.eventDispatcher.listen(listener);
    }

    @Override
    public void unlisten(BalsaEventListener<BalsaSessionEvent> listener)
    {
        this.eventDispatcher.unlisten(listener);
    }
    
    
}