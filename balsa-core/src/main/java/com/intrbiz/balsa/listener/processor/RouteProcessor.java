package com.intrbiz.balsa.listener.processor;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.RouteEngine;
import com.intrbiz.balsa.error.BalsaIOError;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.listener.BalsaProcessor;

public final class RouteProcessor implements BalsaProcessor
{
    private final RouteEngine engine;

    private Logger logger = Logger.getLogger(RouteProcessor.class);

    public RouteProcessor(RouteEngine engine)
    {
        this.engine = engine;
    }

    public void process(BalsaContext context) throws Throwable
    {
        try
        {
            try
            {
                try
                {
                    // Route the request
                    this.engine.route(context);
                }
                catch (Error e)
                {
                    // We should not trap VM errors!
                    throw e;
                }
                catch (BalsaInternalError | BalsaIOError | IOException error)
                {
                    // Errors which cannot be handled
                    throw error;
                }
                catch (Throwable t)
                {
                    try
                    {
                        // Process the exception
                        context.setException(t);
                        logger.debug("Caught exception, applying exception routing", t);
                        this.engine.routeException(context, t);
                    }
                    catch (Error e)
                    {
                        // We should not trap VM errors!
                        throw e;
                    }
                    catch (BalsaInternalError | BalsaIOError | IOException error)
                    {
                        // errors which cannot be handled
                        throw error;
                    }
                    catch (Throwable tt)
                    {
                        throw new BalsaInternalError("Error while processing exception handler", tt);
                    }
                }
            }
            catch (BalsaInternalError | BalsaIOError | IOException error)
            {
                // We have encountered an error this application cannot handle
                // Log
                logger.debug("An exception was encountered which the application cannot deal with:", error);
                logger.debug("Request info:\r\n" + context.request().dump());
                // Don't output anything, let the web server handle the error
            }
        }
        finally
        {
            // ensure the response is flushed
            context.response().flush();
        }
    }
    
    public String toString()
    {
        return "Route Processor";
    }
}
