package com.intrbiz.balsa.view.core.html;

import java.io.IOException;
import java.util.Map.Entry;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.express.ELException;
import com.intrbiz.express.value.ValueExpression;

public class ARenderer extends GenericRenderer
{
    protected void encodeAttributes(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        for (Entry<String, ValueExpression> attribute : component.getAttributes().entrySet())
        {
            if ("href".equalsIgnoreCase(attribute.getKey()))
            {
                this.encodeHref(component, context, out, attribute.getKey(), attribute.getValue());
            }
            else
            {
                this.encodeAttribute(component, context, out, attribute.getKey(), attribute.getValue());
            }
        }
    }
    
    protected void encodeHref(Component component, BalsaContext context, HTMLWriter out, String name, ValueExpression value) throws IOException, BalsaException
    {
        try
        {
            String href = String.valueOf(value.get(context.getELContext(), this));
            if (href != null && (!(href.startsWith("#") && href.startsWith("/"))))
            {
                href = context.url(href);
            }
            out.attribute(name, href);
        }
        catch (ELException e)
        {
            throw new BalsaException("EL error", e);
        }
    }
}
