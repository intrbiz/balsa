package com.intrbiz.balsa.view.core.html;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.express.ELException;

public class PreRenderer extends GenericRenderer
{
    public void encodeStart(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        out.openStartTag(component.getName());
        // attributes
        this.encodeAttributes(component, context, out);
        out.closeStartTag();
        // text
        this.encodeText(component, context, out);
    }
    
    @Override
    protected void encodeText(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        if (component.getText() != null)
        {
            try
            {
                out.putEnc(String.valueOf(component.getText().get(context.getELContext(), this)));
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
        out.endTagLn(component.getName());
    }
}
