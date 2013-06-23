package com.intrbiz.balsa.view.core.html;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.express.ExpressException;

public class StyleRenderer extends GenericRenderer
{
    @Override
    protected void encodeText(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        out.putPadLn("/* <![CDATA[ */");
        if (component.getText() != null)
        {
            try
            {
                out.putPadLn(String.valueOf(component.getText().get(context.getExpressContext(), this)));
            }
            catch (ExpressException e)
            {
                throw new BalsaException("EL error", e);
            }
        }
        out.putPadLn("/* ]]> */");
    }
}
