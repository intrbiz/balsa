package com.intrbiz.balsa.view.renderer;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.ExpressException;

public abstract class Renderer
{
    public void decodeStart(Component component, BalsaContext context) throws BalsaException, ExpressException
    {
    }

    public void decodeChildren(Component component, BalsaContext context) throws BalsaException, ExpressException
    {
        for (Component child : component.getChildren())
        {
            child.decode(context);
        }
    }

    public void decodeEnd(Component component, BalsaContext context) throws BalsaException, ExpressException
    {
    }

    public void encodeStart(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
    }

    public void encodeChildren(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        for (Component child : component.getChildren())
        {
            child.encode(context, to);
        }
    }

    public void encodeEnd(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
    }
    
    public void load(Component component, BalsaContext context) throws BalsaException
    {
    }
}
