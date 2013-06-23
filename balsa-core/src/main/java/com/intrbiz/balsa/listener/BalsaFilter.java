package com.intrbiz.balsa.listener;

import com.intrbiz.balsa.BalsaContext;

/**
 * Filter a Balsa request
 */
public interface BalsaFilter
{
    /**
     * A view onto the next BalsaFilter or BalsaProcessor in the chain
     */
    public static interface BalsaFilterChain
    {
        void filter(BalsaContext context) throws Throwable;
    }
    
    /**
     * Filter a Balsa context.
     * 
     * <p>
     * Use <code>next.filter(context)</code> to invoke the next filter or processor in the chain
     * </p>
     * 
     * @param context the Balsa context
     * @param next the next filter or processor in the chain
     * @throws Throwable
     * returns void
     */
    void filter(BalsaContext context, BalsaFilterChain next) throws Throwable;
}
