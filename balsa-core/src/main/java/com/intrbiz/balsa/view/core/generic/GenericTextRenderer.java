package com.intrbiz.balsa.view.core.generic;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.HTMLRenderer;
import com.intrbiz.express.ExpressException;

public class GenericTextRenderer extends HTMLRenderer
{

    @Override
    public void encodeStart(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        // text
        this.encodeText(component, context, out);
    }

    protected void encodeText(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        if (component.getText() != null)
        {
            try
            {
                out.putEncPadLn(String.valueOf(component.getText().get(context.getExpressContext(), component)));
            }
            catch (ExpressException e)
            {
                throw new BalsaException("EL error", e);
            }
        }
    }

    @Override
    public void encodeEnd(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
    }

}
