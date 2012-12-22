package com.intrbiz.balsa.listener.processor;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.RouteEngine;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.listener.BalsaProcessor;

public class RouteProcessor implements BalsaProcessor
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
                // Route the request
                this.engine.route(context);
            }
            catch (Error e)
            {
                // We should not trap VM errors!
                throw e;
            }
            catch (IOException e)
            {
                // IO Error talking to the web server, nothing we can do!
                throw e;
            }
            catch (BalsaInternalError jie)
            {
                // Don't invoke the error handler in the event of a framework error
                throw jie;
            }
            catch (Throwable t)
            {
                try
                {
                    // Process the exception
                    context.setException(t);
                    logger.warn("Caught exception applying exception routing", t);
                    this.engine.routeException(context, t);
                }
                catch (Error e)
                {
                    // We should not trap VM errors!
                    throw e;
                }
                catch (IOException e)
                {
                    // IO Error talking to the web server, nothing we can do!
                    throw e;
                }
                catch (BalsaInternalError jie)
                {
                    // Don't invoke the error handler in the event of a framework error
                    throw jie;
                }
                catch (Throwable tt)
                {
                    throw new BalsaInternalError("Error while processing exception handler", tt);
                }
            }
        }
        catch (IOException e)
        {
            // IO Error talking to the web server, nothing we can do!
            throw e;
        }
        catch (BalsaInternalError jie)
        {
            // We have encountered an error this application cannot handle
            // Log
            logger.error("An exception was encountered which the application cannot deal with:", jie);
            logger.error("Request info:\r\n" + context.getRequest().dumpRequest());
            // Don't output anything, let the web server handle the error
        }
    }
}
