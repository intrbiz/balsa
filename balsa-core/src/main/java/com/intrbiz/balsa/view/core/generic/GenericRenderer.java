package com.intrbiz.balsa.view.core.generic;

import java.io.IOException;
import java.util.Map.Entry;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.HTMLRenderer;
import com.intrbiz.express.ELException;
import com.intrbiz.express.value.ValueExpression;

public class GenericRenderer extends HTMLRenderer
{

    @Override
    public void encodeStart(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        out.openStartTagPad(component.getName());
        // attributes
        this.encodeAttributes(component, context, out);
        out.closeStartTagLn();
        // text
        this.encodeText(component, context, out);
    }

    protected void encodeAttributes(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        for (Entry<String, ValueExpression> attribute : component.getAttributes().entrySet())
        {
            this.encodeAttribute(component, context, out, attribute.getKey(), attribute.getValue());
        }
    }

    protected void encodeAttribute(Component component, BalsaContext context, HTMLWriter out, String name, ValueExpression value) throws IOException, BalsaException
    {
        try
        {
            out.attribute(name, String.valueOf(value.get(context.getELContext(), this)));
        }
        catch (ELException e)
        {
            throw new BalsaException("EL error", e);
        }
    }

    protected void encodeText(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        if (component.getText() != null)
        {
            try
            {
                out.putEncPadLn(String.valueOf(component.getText().get(context.getELContext(), this)));
            }
            catch (ELException e)
            {
                throw new BalsaException("EL error", e);
            }
        }
    }

    @Override
    public void encodeEnd(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        out.endTagPadLn(component.getName());
    }

}
