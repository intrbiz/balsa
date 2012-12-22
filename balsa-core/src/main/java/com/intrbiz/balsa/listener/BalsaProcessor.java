package com.intrbiz.balsa.listener;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;

/**
 * A Balsa request processor
 */
public interface BalsaProcessor
{
    /**
     * Process an Balsa context
     * @param context the Balsa context
     * @throws IOException
     * returns void
     */
    public void process(BalsaContext context) throws Throwable;
}
