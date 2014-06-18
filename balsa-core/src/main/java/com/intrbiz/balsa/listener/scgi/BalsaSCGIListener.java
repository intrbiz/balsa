package com.intrbiz.balsa.listener.scgi;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonFactory;
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
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

public class BalsaSCGIListener extends BalsaListener
{
    public static final int DEFAULT_PORT = 8090;
    
    private SCGIListener listener;
    
    private final Counter totalRequests;
    
    private final Counter activeRequests;
    
    private final Meter requests;
    
    private final Timer duration;
    
    private JsonFactory jsonFactory = new JsonFactory();
    
    private XMLOutputFactory xmlOutFactory = XMLOutputFactory.newFactory();
    
    private XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();
    
    public BalsaSCGIListener(int port, int poolSize)
    {
        super(port, poolSize);
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.balsa");
        this.totalRequests = source.getRegistry().counter(Witchcraft.name(BalsaListener.class, "total-requests"));
        this.activeRequests = source.getRegistry().counter(Witchcraft.name(BalsaListener.class, "active-requests"));
        this.requests = source.getRegistry().meter(Witchcraft.name(BalsaListener.class, "requests"));
        this.duration = source.getRegistry().timer(Witchcraft.name(BalsaListener.class, "request-duration"));
    }

    public BalsaSCGIListener(int port)
    {
        this(port, SCGIListener.DEFAULT_POOL_SIZE);
    }
    
    public BalsaSCGIListener()
    {
        this(DEFAULT_PORT);
    }

    public String getEngineName()
    {
        return "Balsa-SCGI-Listener";
    }
    
    public int getDefaultPort()
    {
        return DEFAULT_PORT;
    }
    
    public String getListenerType()
    {
        return "scgi";
    }

    @Override
    public void start() throws BalsaException
    {
        this.listener = new SCGIListener(this.getPort(), this.getPoolSize());
        //
        final BalsaApplication app = this.getBalsaApplication();
        final BalsaProcessor proc = this.getProcessor();
        // the processor
        SCGIProcessor processor = new SCGIProcessor()
        {
            @Override
            public void process(SCGIRequest request, SCGIResponse response) throws Throwable
            {
                // bridge
                BalsaRequest req = new BalsaSCGIRequest(request, jsonFactory, xmlInFactory);
                BalsaResponse res = new BalsaSCGIResponse(response, req, jsonFactory, xmlOutFactory);
                //
                BalsaContext ctx = new BalsaContext(app, req, res);
                BalsaContext.set(ctx);
                //
                Timer.Context tctx = duration.time();
                totalRequests.inc();
                activeRequests.inc();
                requests.mark();
                try
                {
                    proc.process(ctx);
                }
                finally
                {
                    tctx.stop();
                    activeRequests.dec();
                }
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
