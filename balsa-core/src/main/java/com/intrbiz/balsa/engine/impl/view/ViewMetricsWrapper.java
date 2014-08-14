package com.intrbiz.balsa.engine.impl.view;

import java.io.IOException;

import com.codahale.metrics.Timer;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.ViewEngine;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.engine.view.ViewMetadata;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.express.ExpressException;
import com.intrbiz.gerald.witchcraft.Witchcraft;

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
        this.encodeTimer = Witchcraft.get().source("com.intrbiz.balsa").getRegistry().timer(Witchcraft.scoped(ViewEngine.class, "encode", this.id));
    }

    public void decode(BalsaContext context) throws BalsaException, ExpressException
    {
        realView.decode(context);
    }

    public void encode(BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        Timer.Context tCtx = this.encodeTimer.time();
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

    @Override
    public ViewMetadata getMetadata()
    {
        return realView.getMetadata();
    }
}
