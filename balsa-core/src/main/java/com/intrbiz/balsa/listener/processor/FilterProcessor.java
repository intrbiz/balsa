package com.intrbiz.balsa.listener.processor;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaFilter;
import com.intrbiz.balsa.listener.BalsaFilter.BalsaFilterChain;
import com.intrbiz.balsa.listener.BalsaProcessor;

public final class FilterProcessor implements BalsaProcessor
{
    private final BalsaFilter filter;
    
    private final BalsaFilterChain chain;
    
    public FilterProcessor(BalsaFilter filter, final BalsaProcessor next)
    {
        super();
        if (filter == null) throw new IllegalArgumentException("The filter cannot be null");
        if (next == null) throw new IllegalArgumentException("The next processor cannot be null");
        this.filter = filter;
        this.chain = new BalsaFilterChain() {
            @Override
            public void filter(BalsaContext context) throws Throwable
            {
                next.process(context);
            }
        };
    }
    
    @Override
    public void process(BalsaContext context) throws Throwable
    {
        this.filter.filter(context, this.chain);
    }
}
