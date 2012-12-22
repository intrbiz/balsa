package com.intrbiz.balsa.view.core.template;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;

public class ContentRenderer extends Renderer
{
    @Override
    public void decodeChildren(Component component, BalsaContext context) throws BalsaException
    {
        
    }

    @Override
    public void encodeChildren(Component component, BalsaContext context) throws IOException, BalsaException
    {
        // Get the next view in the chain
        if (component.getView() != null && component.getView().getNext() != null && component.getView().getNext().getRoot() != null)
        {
            component.getView().getNext().getRoot().encode(context);
        }
    }
}
