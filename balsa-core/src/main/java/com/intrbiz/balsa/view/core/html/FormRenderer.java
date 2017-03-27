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

public class FormRenderer extends GenericRenderer
{
    
    protected void encodeAttributes(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        if (component.hasAttribute("path"))
        {
            for (Entry<String, ValueExpression> attribute : component.getAttributes().entrySet())
            {
                if ("path".equals(attribute.getKey()))
                {
                    // compute the full path
                    this.encodeHref(component, context, out, "action", attribute.getValue());
                    if (! component.hasAttribute("method"))
                        out.attribute("method", "post");
                }
                else if (! ("access-token".equals(attribute.getKey()) || "action".equals(attribute.getKey())))
                {
                    this.encodeAttribute(component, context, out, attribute.getKey(), attribute.getValue());
                }
            }
        }
        else
        {
            for (Entry<String, ValueExpression> attribute : component.getAttributes().entrySet())
            {
                if (! ("access-token".equals(attribute.getKey())))
                {
                    this.encodeAttribute(component, context, out, attribute.getKey(), attribute.getValue());
                }
            }
        }
    }

    @Override
    public void encodeChildren(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException, ExpressException
    {
        // inject our access token
        if (component.hasAttribute("path"))
        {
            String id = ((FormComponent) component).getAccessToken();
            String path = this.processHrefPath(component, context, component.getAttribute("path"));
            // inject the input
            out.openStartTagPad("input");
            out.attribute("type", "hidden");
            out.attribute("name", id);
            out.attribute("value", context.generateAccessTokenForURL(path));
            out.closeStartTagLn();
            out.endTagLn("input");
        }
        // encode our children
        super.encodeChildren(component, context, out);
    }
}
