package com.intrbiz.balsa.view.core.html;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.express.ELException;

public class ScriptRenderer extends GenericRenderer
{
    @Override
    protected void encodeText(Component component, BalsaContext context, HTMLWriter out) throws IOException, BalsaException
    {
        out.putPadLn("/* <![CDATA[ */");
        if (component.getText() != null)
        {
            try
            {
                out.putPadLn(String.valueOf(component.getText().get(context.getELContext(), this)));
            }
            catch (ELException e)
            {
                throw new BalsaException("EL error", e);
            }
        }
        out.putPadLn("/* ]]> */");
    }
}
