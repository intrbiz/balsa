package com.intrbiz.balsa.view.renderer;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.balsa.view.component.Component;

public abstract class HTMLRenderer extends Renderer
{
    @Override
    public void encodeStart(Component component, BalsaContext context) throws IOException, BalsaException
    {
        this.encodeStart(component, context, context.getResponse().getHtmlWriter());
    }

    @Override
    public void encodeEnd(Component component, BalsaContext context) throws IOException, BalsaException
    {
        this.encodeEnd(component, context, context.getResponse().getHtmlWriter());
    }
    
    public abstract void encodeStart(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException;
    public abstract void encodeEnd(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException;
}
