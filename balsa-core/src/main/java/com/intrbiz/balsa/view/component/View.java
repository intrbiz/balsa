package com.intrbiz.balsa.view.component;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;

public class View implements BalsaView
{
    private Component root;

    private View previous;

    private View next;

    public View(View previous)
    {
        super();
        this.previous = previous;
        if (this.previous != null) this.previous.setNext(this);
    }

    public Component getRoot()
    {
        return root;
    }

    public void setRoot(Component root)
    {
        this.root = root;
    }

    public View getPrevious()
    {
        return previous;
    }

    public void setPrevious(View previous)
    {
        this.previous = previous;
        if (this.previous != null) this.previous.setNext(this);
    }

    public View getNext()
    {
        return next;
    }

    public void setNext(View next)
    {
        this.next = next;
    }

    @Override
    public void decode(BalsaContext context) throws BalsaException
    {
        this.root.decode(context);
    }

    @Override
    public void encode(BalsaContext context) throws IOException, BalsaException
    {
        this.root.encode(context);
    }
}
