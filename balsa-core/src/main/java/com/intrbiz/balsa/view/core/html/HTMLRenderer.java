package com.intrbiz.balsa.view.core.html;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;

public class HTMLRenderer extends GenericRenderer
{
    @Override
    public void encodeStart(Component component, BalsaContext context) throws IOException, BalsaException
    {
        context.getResponse().html();
        this.encodeStart(component, context, context.getResponse().getHtmlWriter());
    }
}
