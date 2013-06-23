package com.intrbiz.balsa.listener;

import com.intrbiz.balsa.BalsaContext;

/**
 * A Balsa request processor
 */
public interface BalsaProcessor
{
    /**
     * Process a Balsa context
     * @param context the Balsa context
     * @throws Throwable
     * returns void
     */
    void process(BalsaContext context) throws Throwable;
}
