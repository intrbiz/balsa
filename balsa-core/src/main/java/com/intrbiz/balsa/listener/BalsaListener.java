package com.intrbiz.balsa.listener;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.BalsaEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.util.IBThreadFactory;

public abstract class BalsaListener extends AbstractBalsaEngine implements Runnable, BalsaEngine
{
    public static final int DEFAULT_PORT = 8090;

    public static final int DEFAULT_POOL_SIZE = 16;

    private int port = DEFAULT_PORT;

    private int poolSize = DEFAULT_POOL_SIZE;

    protected BalsaProcessor processor;

    private volatile boolean run = false;

    private ServerSocket server;
    
    private Thread listenerThread;

    private ThreadFactory workerFactory;

    private BalsaWorker[] workers;

    private BlockingQueue<Socket> runQueue;

    private Logger logger = Logger.getLogger(BalsaListener.class);

    public BalsaListener()
    {
        super();
    }

    public BalsaListener(int port)
    {
        this();
        this.port = port;
    }

    public BalsaListener(int port, int poolSize)
    {
        this();
        this.port = port;
        this.poolSize = poolSize;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public int getPoolSize()
    {
        return poolSize;
    }

    public void setPoolSize(int poolSize)
    {
        this.poolSize = poolSize;
    }

    public BalsaProcessor getProcessor()
    {
        return processor;
    }

    public void setProcessor(BalsaProcessor processor)
    {
        this.processor = processor;
    }

    boolean isRun()
    {
        return this.run;
    }

    protected abstract BalsaWorker createWorker(BalsaListener listener, BlockingQueue<Socket> runQueue, ThreadFactory workerFactory);

    public void start() throws BalsaException
    {
        try
        {
            this.listenerThread = new Thread(new ThreadGroup("Balsa"), this, "BalsaListener");
            // factory
            this.workerFactory = new IBThreadFactory("BalsaWorker", true, new ThreadGroup("Balsa"));
            // runqueue
            this.runQueue = new LinkedBlockingQueue<Socket>(this.getPoolSize() * 2);
            // prefork
            this.run = true;
            this.workers = new BalsaWorker[this.getPoolSize()];
            for (int i = 0; i < this.workers.length; i++)
            {
                this.workers[i] = this.createWorker(this, this.runQueue, this.workerFactory);
                this.workers[i].start();
            }
            // listen
            this.server = new ServerSocket(this.getPort());
            this.server.setSoTimeout(20000);
            // start the listener
            this.listenerThread.start();
        }
        catch (BindException e)
        {
            throw new BalsaException("Failed to start SCGI Listener, could not bind to socket", e);
        }
        catch (IOException e)
        {
            throw new BalsaException("Failed to start SCGI Listener", e);
        }
    }

    public void run()
    {
        while (this.run)
        {
            try
            {
                // Place the socket onto the run queue
                this.runQueue.offer(this.server.accept());
            }
            catch (SocketTimeoutException e)
            {
                /* expected */
            }
            catch (IOException e)
            {
                logger.fatal("Error during listener run loop", e);
            }
        }
    }

    public void shutdown()
    {
        this.stop();
        if (this.workers != null)
        {
            for (BalsaWorker worker : this.workers)
            {
                worker.await();
            }
        }
    }

    public void stop()
    {
        if (this.server != null)
        {
            try
            {
                this.run = false;
                this.runQueue.notifyAll();
                this.server.close();
            }
            catch (Exception e)
            {
            }
        }
    }
}
