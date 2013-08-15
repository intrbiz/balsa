package com.intrbiz.balsa.engine.view;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.express.ExpressException;

public abstract class BalsaView
{
    private BalsaView previous;

    private BalsaView next;

    private final ViewMetadata metadata = new ViewMetadata();

    public BalsaView(BalsaView previous)
    {
        super();
        this.previous = previous;
        if (this.previous != null) this.previous.setNext(this);
    }

    public abstract void decode(BalsaContext context) throws BalsaException, ExpressException;

    public abstract void encode(BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException;

    // The view chain

    public BalsaView getPrevious()
    {
        return previous;
    }

    public void setPrevious(BalsaView previous)
    {
        this.previous = previous;
        if (this.previous != null) this.previous.setNext(this);
    }

    public BalsaView getNext()
    {
        return next;
    }

    public void setNext(BalsaView next)
    {
        this.next = next;
    }

    public BalsaView getHead()
    {
        if (this.previous == null) return this;
        return this.previous.getHead();
    }

    public BalsaView getTail()
    {
        if (this.next == null) return this;
        return this.next.getTail();
    }

    // meta data

    public abstract String getTitle();

    public ViewMetadata getMetadata()
    {
        return metadata;
    }
}
