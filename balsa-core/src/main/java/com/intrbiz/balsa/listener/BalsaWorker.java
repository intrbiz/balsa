package com.intrbiz.balsa.listener;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;

public abstract class BalsaWorker implements Runnable
{
    private final BalsaListener listener;

    private final BlockingQueue<Socket> runQueue;

    private final Thread thread;
    
    private Logger logger = Logger.getLogger(this.getClass());

    public BalsaWorker(BalsaListener listener, BlockingQueue<Socket> runQueue, ThreadFactory workerFactory)
    {
        super();
        this.listener = listener;
        this.runQueue = runQueue;
        this.thread = workerFactory.newThread(this);
    }
    
    protected BalsaProcessor getProcessor()
    {
        return this.listener.getProcessor();
    }
    
    protected abstract BalsaContext createbalsaContext(BalsaApplication application);

    public void run()
    {
        // The client
        Socket client;
        // Our context
        BalsaContext context = this.createbalsaContext(this.listener.getBalsaApplication());
        BalsaContext.set(context);
        // Enter the run loop
        while (this.listener.isRun())
        {
            try
            {
                client = this.runQueue.take();
                try
                {
                    this.runClient(client, context);
                }
                catch (Throwable t)
                {
                    // A throwable should not reach here - Fatal!
                    if (t instanceof OutOfMemoryError) logger.fatal("OUT OF MEMORY ERROR!");
                    logger.fatal("Terminated request due to uncaught throwable while processing request!", t);
                }
            }
            catch (InterruptedException e)
            {
                /* expected */
            }
        }
        // Unbind from the thread
        BalsaContext.set(null);
    }
    
    protected abstract void runClient(Socket client, BalsaContext context) throws Throwable;
    
    public void start()
    {
        this.thread.start();
    }
    
    public void await()
    {
        try
        {
            this.thread.join();
        }
        catch (InterruptedException e)
        {
        }
    }
}
