package com.intrbiz.balsa.view.core.generic;

import java.io.IOException;
import java.util.Map.Entry;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.HTMLRenderer;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;

public class GenericRenderer extends HTMLRenderer
{

    @Override
    public void encodeStart(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        out.openStartTagPad(component.getName());
        // attributes
        this.encodeAttributes(component, context, out);
        out.closeStartTagLn();
        // text
        this.encodeText(component, context, out);
    }

    protected void encodeAttributes(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        for (Entry<String, ValueExpression> attribute : component.getAttributes().entrySet())
        {
            if (! Component.RENDERED.equals(attribute.getKey()))
            {
                this.encodeAttribute(component, context, out, attribute.getKey(), attribute.getValue());
            }
        }
    }

    protected void encodeAttribute(Component component, BalsaContext context, BalsaWriter out, String name, ValueExpression value) throws IOException, BalsaException
    {
        Object theValue = value.get(context.getExpressContext(), component);
        if (theValue != null)
            out.attribute(name, theValue.toString());
    }
    
    protected String processHref(Component component, BalsaContext context, ValueExpression value)
    {
        Object val = value.get(context.getExpressContext(), component);
        String href = val == null ? null : val.toString();
        if (href != null && (!(href.startsWith("#") && href.startsWith("/"))))
        {
            href = context.url(href);
        }
        return href;
    }
    
    protected String processHrefPath(Component component, BalsaContext context, ValueExpression value)
    {
        Object val = value.get(context.getExpressContext(), component);
        String href = val == null ? null : val.toString();
        if (href != null && (!(href.startsWith("#") && href.startsWith("/"))))
        {
            href = context.path(href);
        }
        return href;
    }
    
    protected void encodeHref(Component component, BalsaContext context, BalsaWriter out, String name, ValueExpression value) throws IOException, BalsaException
    {
        try
        {
            out.attribute(name, processHref(component, context, value));
        }
        catch (ExpressException e)
        {
            throw new BalsaException("EL error", e);
        }
    }

    protected void encodeText(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        if (component.getText() != null)
        {
            Object theText = component.getText().get(context.getExpressContext(), component);
            if (theText != null)
                out.putEncPadLn(theText.toString());
        }
    }

    @Override
    public void encodeEnd(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        out.endTagPadLn(component.getName());
    }

}
