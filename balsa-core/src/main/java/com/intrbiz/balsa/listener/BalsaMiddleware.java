package com.intrbiz.balsa.listener;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;

public interface BalsaMiddleware
{
    /**
     * Process an Balsa context before it is processed
     * @param context the Balsa context
     * @throws IOException
     * returns boolean - continue processing
     */
    public boolean before(BalsaContext context) throws Throwable;
    
    /**
     * Process an Balsa context after it has been processed
     * @param context the Balsa context
     * @throws IOException
     * returns void
     */
    public void after(BalsaContext context) throws Throwable;
}
