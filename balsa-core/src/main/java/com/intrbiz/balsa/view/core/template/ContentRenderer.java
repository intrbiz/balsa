package com.intrbiz.balsa.view.core.template;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.express.ExpressException;

public class ContentRenderer extends Renderer
{
    @Override
    public void decodeChildren(Component component, BalsaContext context) throws BalsaException
    {
        
    }

    @Override
    public void encodeChildren(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        // Get the next view in the chain
        if (component.getView() != null && component.getView().getNext() != null)
        {
            component.getView().getNext().encode(context, to);
        }
    }
}
