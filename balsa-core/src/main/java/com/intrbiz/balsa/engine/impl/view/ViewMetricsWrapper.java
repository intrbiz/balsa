package com.intrbiz.balsa.engine.impl.view;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.ViewEngine;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.express.ExpressException;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class ViewMetricsWrapper extends BalsaView
{
    private final String id;
    private final Timer encodeTimer;
    private final BalsaView realView;
    
    public ViewMetricsWrapper(String id, BalsaView realView)
    {
        super(null);
        this.id = id;
        this.realView = realView;
        this.encodeTimer = Metrics.newTimer(ViewEngine.class, "encode", this.id, TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    public void decode(BalsaContext context) throws BalsaException, ExpressException
    {
        realView.decode(context);
    }

    public void encode(BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        TimerContext tCtx = this.encodeTimer.time();
        try
        {
            realView.encode(context, to);
        }
        finally
        {
            tCtx.stop();
        }
    }

    public BalsaView getPrevious()
    {
        return realView.getPrevious();
    }

    public void setPrevious(BalsaView previous)
    {
        realView.setPrevious(previous);
    }

    public BalsaView getNext()
    {
        return realView.getNext();
    }

    public void setNext(BalsaView next)
    {
        realView.setNext(next);
    }

    public BalsaView getHead()
    {
        return realView.getHead();
    }

    public BalsaView getTail()
    {
        return realView.getTail();
    }

    public String getTitle()
    {
        return realView.getTitle();
    }
    
    public String toString()
    {
        return "BalsaView[" + this.id + "]";
    }
}
