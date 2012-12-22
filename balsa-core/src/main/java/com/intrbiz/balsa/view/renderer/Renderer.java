package com.intrbiz.balsa.view.renderer;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;

public abstract class Renderer implements Cloneable
{

    public void decodeStart(Component component, BalsaContext context) throws BalsaException
    {
    }

    public void decodeChildren(Component component, BalsaContext context) throws BalsaException
    {
        for (Component child : component.getChildren())
        {
            child.decode(context);
        }
    }

    public void decodeEnd(Component component, BalsaContext context) throws BalsaException
    {
    }

    public void encodeStart(Component component, BalsaContext context) throws IOException, BalsaException
    {
    }

    public void encodeChildren(Component component, BalsaContext context) throws IOException, BalsaException
    {
        for (Component child : component.getChildren())
        {
            child.encode(context);
        }
    }

    public void encodeEnd(Component component, BalsaContext context) throws IOException, BalsaException
    {
    }

    public Renderer cloneRenderer()
    {
        try
        {
            Renderer r = (Renderer) this.clone();

            return r;
        }
        catch (Exception e)
        {
        }
        return null;
    }

    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (Exception e)
        {
        }
        return null;
    }
    
    public void load(Component component, BalsaContext context) throws BalsaException
    {
    }
}
