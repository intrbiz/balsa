package com.intrbiz.balsa.view.core.html;

import java.io.IOException;
import java.util.Map.Entry;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;

public class OptionRenderer extends GenericRenderer
{
    protected void encodeAttributes(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        for (Entry<String, ValueExpression> attribute : component.getAttributes().entrySet())
        {
            if ("selected".equalsIgnoreCase(attribute.getKey()))
            {
                this.encodeSelected(component, context, out, attribute.getKey(), attribute.getValue());
            }
            else
            {
                this.encodeAttribute(component, context, out, attribute.getKey(), attribute.getValue());
            }
        }
    }
    
    protected void encodeSelected(Component component, BalsaContext context, BalsaWriter out, String name, ValueExpression value) throws IOException, BalsaException
    {
        try
        {
            Object selectedValue = value.get(context.getExpressContext(), this);
            if (selectedValue instanceof Boolean)
            {
                if (((Boolean) selectedValue).booleanValue())
                    out.attribute(name, "selected");
            }
            else
            {
                out.attribute(name, String.valueOf(selectedValue));
            }
        }
        catch (ExpressException e)
        {
            throw new BalsaException("EL error", e);
        }
    }
}
