package com.intrbiz.balsa.test.session;

import static org.junit.Assert.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.SessionEngine;
import com.intrbiz.balsa.engine.impl.session.SimpleSession;
import com.intrbiz.balsa.engine.session.BalsaSession;

public class SessionTests
{
    private BalsaApplication application;

    private SessionEngine sessionEngine;

    @Before
    public void setUp() throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.FATAL);
        //
        this.application = new BalsaApplication()
        {
            @Override
            protected void setup() throws BalsaException
            {
            }
        };
        this.sessionEngine = this.application.getSessionEngine();
        this.sessionEngine.start();
    }

    @Test
    public void makeSessionId()
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull("Created a session id", sessionId);
        assertTrue("Session ids are long enough (" + sessionId.length() + " >= 32)", sessionId.length() >= 32);
    }

    @Test
    public void createSession()
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull(sessionId);
        BalsaSession session = this.sessionEngine.getSession(sessionId);
        assertNotNull("Created a session", session);
        assertSame("Session id equals session.id()", sessionId, session.id());
    }
    
    @Test
    public void getSession()
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull(sessionId);
        BalsaSession session1 = this.sessionEngine.getSession(sessionId);
        assertNotNull("Created a session", session1);
        BalsaSession session2 = this.sessionEngine.getSession(sessionId);
        assertNotNull("Got the session", session2);
        //
        assertSame("Sessions are the same", session1.id(), session2.id());
    }
    
    @Test
    public void sessionAddVar()
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull(sessionId);
        BalsaSession session = this.sessionEngine.getSession(sessionId);
        assertNotNull(session);
        //
        String name  = "test-1";
        String value = "Testing 123";
        // put
        session.var(name, value);
        // get
        Object o = session.var(name);
        assertNotNull("Got var", o);
        assertTrue("Got var of correct type", o instanceof String);
        assertSame("Got same var", o, value);
        //
        String s = session.var(name);
        assertNotNull("Got var", s);
        assertTrue("Got var of correct type", s instanceof String);
        assertSame("Got same var", s, value);
    }
    
    @Test
    public void sessionAddNullVar()
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull(sessionId);
        BalsaSession session = this.sessionEngine.getSession(sessionId);
        assertNotNull(session);
        //
        String name  = "test-1";
        String value = null;
        // put
        session.var(name, value);
        // get
        Object o = session.var(name);
        assertTrue("Got null var", o == null);
        String s = session.var(name);
        assertTrue("Got null var", s == null);
    }
    
    @Test
    public void sessionAddVarNullName()
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull(sessionId);
        BalsaSession session = this.sessionEngine.getSession(sessionId);
        assertNotNull(session);
        //
        try
        {
            session.var(null, new Object());
            fail("Null name did not error");
        }
        catch (IllegalArgumentException e)
        {
        }
        try
        {
            session.var(null);
            fail("Null name did not error");
        }
        catch (IllegalArgumentException e)
        {
        }
        try
        {
            session.var(null, String.class);
            fail("Null name did not error");
        }
        catch (IllegalArgumentException e)
        {
        }
    }
    
    @Test
    public void sessionVarSeparateFromModel()
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull(sessionId);
        BalsaSession session = this.sessionEngine.getSession(sessionId);
        assertNotNull(session);
        //
        String name  = "test-var-1";
        String value = "Testing 123";
        // put
        session.var(name, value);
        // get
        String s = session.var(name);
        assertSame("Got same var", s, value);
        //
        String m = session.model(name);
        assertNull("Model cannot get var", m);
    }
    
    @Test
    public void sessionAccessTimes() throws InterruptedException
    {
        String sessionId = this.sessionEngine.makeId();
        assertNotNull(sessionId);
        BalsaSession session1 = this.sessionEngine.getSession(sessionId);
        assertNotNull(session1);
        //
        if (session1 instanceof SimpleSession)
        {
            //
            long t1 = ((SimpleSession) session1).lastAccess();
            assertTrue("Access time > 0", t1 > 0);
            //
            Thread.sleep(50);
            //
            BalsaSession session2 = this.sessionEngine.getSession(sessionId);
            assertNotNull(session2);
            //
            if (session2 instanceof SimpleSession)
            {
                //
                long t2 = ((SimpleSession) session2).lastAccess();
                assertTrue("Access time > 0", t2 > 0);
                assertTrue("Access time t2 > t1", t2 > t1);
                //
                Thread.sleep(50);
                //
                ((SimpleSession) session2).access();
                long t3 = ((SimpleSession) session2).lastAccess();
                assertTrue("Access time > 0", t3 > 0);
                assertTrue("Access time t3 > t2", t3 > t2);
            }
        }
    }

    @After
    public void tearDown() throws Exception
    {
        this.sessionEngine.stop();
        this.sessionEngine = null;
        this.application = null;
    }
}
