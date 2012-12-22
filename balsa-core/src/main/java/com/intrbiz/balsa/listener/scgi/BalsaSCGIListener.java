package com.intrbiz.balsa.listener.scgi;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.SCGIException;
import com.intrbiz.balsa.listener.BalsaListener;
import com.intrbiz.balsa.listener.BalsaProcessor;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.scgi.SCGIListener;
import com.intrbiz.balsa.scgi.SCGIProcessor;
import com.intrbiz.balsa.scgi.SCGIRequest;
import com.intrbiz.balsa.scgi.SCGIResponse;
import com.intrbiz.balsa.scgi.middleware.CookieMiddleware;
import com.intrbiz.balsa.scgi.middleware.LoggingMiddleware;
import com.intrbiz.balsa.scgi.middleware.MiddlewareProcessor;
import com.intrbiz.balsa.scgi.middleware.QueryStringMiddleware;

public class BalsaSCGIListener extends BalsaListener
{
    private SCGIListener listener;
    
    public BalsaSCGIListener()
    {
        super();
    }

    public BalsaSCGIListener(int port, int poolSize)
    {
        super(port, poolSize);
    }

    public BalsaSCGIListener(int port)
    {
        super(port);
    }

    @Override
    public void start() throws BalsaException
    {
        this.listener = new SCGIListener(this.getPort(), this.getPoolSize());
        //
        final BalsaApplication app = this.getBalsaApplication();
        final BalsaProcessor proc = this.getProcessor();
        // the processor
        SCGIProcessor processor = new SCGIProcessor() {
            @Override
            public void process(SCGIRequest request, SCGIResponse response) throws Throwable
            {
                // bridge
                BalsaRequest req = new BalsaSCGIRequest(request);
                BalsaResponse res = new BalsaSCGIResponse(response);
                //
                BalsaContext ctx = new BalsaContext(app, req, res);
                BalsaContext.set(ctx);
                //
                proc.process(ctx);
                //
            }
        };
        // middleware chain
        SCGIProcessor chain = processor;
        chain = new MiddlewareProcessor(new QueryStringMiddleware(), chain);
        chain = new MiddlewareProcessor(new CookieMiddleware(), chain);
        chain = new MiddlewareProcessor(new LoggingMiddleware(), chain);
        //
        this.listener.setProcessor(chain);
        //
        try
        {
            this.listener.start();
        }
        catch (SCGIException e)
        {
            throw new BalsaException("Could not start the SCGI Listener", e);
        }
    }

    @Override
    public void shutdown()
    {
        if (this.listener != null)
        {
            this.listener.shutdown();
        }
    }

    @Override
    public void stop()
    {
        if (this.listener != null)
        {
            this.listener.stop();
        }
    }
}
