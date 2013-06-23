package com.intrbiz.balsa.view.core.template;

import java.io.IOException;
import java.util.List;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.express.ExpressException;

public class IncludeRenderer extends Renderer
{
    @Override
    public void decodeChildren(Component component, BalsaContext context) throws BalsaException
    {
        
    }

    @Override
    public void encodeChildren(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        IncludeComponent inc = (IncludeComponent) component;
        List<String> views = inc.getViews(context);
        if (! views.isEmpty())
        {
            context.encodeOnly(to, views.toArray(new String[0]));
        }
    }
}
