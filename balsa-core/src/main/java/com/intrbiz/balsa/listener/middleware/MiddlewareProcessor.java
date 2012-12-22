package com.intrbiz.balsa.listener.middleware;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaMiddleware;
import com.intrbiz.balsa.listener.BalsaProcessor;

/**
 * Adapt Middleware to a processor, forming a processing chain
 */
public class MiddlewareProcessor implements BalsaProcessor
{
    private final BalsaMiddleware middleware;

    private final BalsaProcessor processor;

    public MiddlewareProcessor(BalsaMiddleware middleware, BalsaProcessor processor)
    {
        super();
        this.middleware = middleware;
        this.processor = processor;
    }

    public void process(BalsaContext context) throws Throwable
    {
        // Call before, abort if false
        if (!this.middleware.before(context)) return;
        try
        {
            this.processor.process(context);
        }
        finally
        {
            // Always call after
            this.middleware.after(context);
        }
    }

}
