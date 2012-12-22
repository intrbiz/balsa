package com.intrbiz.balsa.listener.middleware;

import static com.intrbiz.Util.isEmpty;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaRequest;

public class LoggingMiddleware extends AbstractMiddleware
{
    private Logger logger = Logger.getLogger(LoggingMiddleware.class);

    @Override
    public boolean before(BalsaContext context) throws IOException
    {
        context.setProcessingStart(System.nanoTime());
        return true;
    }

    @Override
    public void after(BalsaContext context) throws IOException
    {
        context.setProcessingEnd(System.nanoTime());
        // Log to trace
        if (logger.isDebugEnabled())
        {
            BalsaRequest request = context.getRequest();
            logger.debug("Processing request: " + request.getPathInfo() + (isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString()) + " " + request.getRequestMethod() + " in " + ((context.getProcessingEnd() - context.getProcessingStart())/1000) + "us");
        }
    }

}
