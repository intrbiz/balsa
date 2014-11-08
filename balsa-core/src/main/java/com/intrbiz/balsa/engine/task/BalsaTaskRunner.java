package com.intrbiz.balsa.engine.task;

import java.io.Serializable;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.session.BalsaSession;

public class BalsaTaskRunner implements Runnable, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String sessionId;
    
    private final String id;
    
    private final BalsaTask task;
    
    public BalsaTaskRunner(String sessionId, String id, BalsaTask task)
    {
        super();
        this.sessionId = sessionId;
        this.id = id;
        this.task = task;
    }

    @Override
    public void run()
    {
        // get the application
        BalsaApplication application = BalsaApplication.getInstance();
        // get the session
        BalsaSession session = application.getSessionEngine().getSession(this.sessionId);
        // execute the task
        this.task.run(application, session, this.id);
    }
}
