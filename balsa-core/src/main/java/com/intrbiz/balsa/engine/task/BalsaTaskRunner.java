package com.intrbiz.balsa.engine.task;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.session.BalsaSession;

/**
 * Execute a callable within a limited Balsa context, 
 * this allows the callable to use non-request related 
 * aspects of the Balsa context.
 */
public class BalsaTaskRunner implements Runnable, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String sessionId;
    
    private final String id;
    
    private final Callable<?> task;
    
    private Logger logger = Logger.getLogger(BalsaTaskRunner.class);
    
    public BalsaTaskRunner(String sessionId, String id, Callable<?> task)
    {
        super();
        this.sessionId = sessionId;
        this.id = id;
        this.task = task;
    }

    @Override
    public void run()
    {
        try
        {
            // get the application
            BalsaApplication application = BalsaApplication.getInstance();
            // get the session
            BalsaSession session = application.getSessionEngine().getSession(this.sessionId);
            // create a Balsa context
            BalsaContext context = new BalsaContext(application, session);
            try
            {
                // activate the context
                context.activate();
                // bind the context
                context.bind();
                // execute the task
                logger.trace("Executing task in background with restricted balsa context, session: " + session.id());
                try
                {
                    Object result = this.task.call();
                    // update the state
                    session.task(this.id, new BalsaTaskState().complete(result));
                }
                catch (Exception e)
                {
                    // error state
                    session.task(id, new BalsaTaskState().failed(e));
                }
            }
            finally
            {
                // ensure the context is unbound
                context.unbind();
                // deactivate the context
                context.deactivate();
            }
        }
        catch (Exception e)
        {
            logger.fatal("Unhandled error executing balsa task", e);
        }
    }
}
