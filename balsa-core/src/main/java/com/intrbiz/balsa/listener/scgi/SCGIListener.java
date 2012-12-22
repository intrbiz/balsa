package com.intrbiz.balsa.listener.scgi;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

import com.intrbiz.balsa.listener.BalsaListener;
import com.intrbiz.balsa.listener.BalsaWorker;

/**
 * A simple SCGI server
 * 
 * SCGIListener listens on a TCP port and launches workers for the incoming requests.
 */
public class SCGIListener extends BalsaListener
{
    public SCGIListener()
    {
        super();
    }

    protected BalsaWorker createWorker(BalsaListener listener, BlockingQueue<Socket> runQueue, ThreadFactory workerFactory)
    {
        return new SCGIWorker(listener, runQueue, workerFactory);
    }
}
