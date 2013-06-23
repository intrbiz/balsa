package com.intrbiz.balsa.view.core.html;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;

public class HTMLRenderer extends GenericRenderer
{
    @Override
    public void encodeStart(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException
    {
        // doc type
        to.write("<!DOCTYPE html>\r\n");
        // encode this tag
        super.encodeStart(component, context, to);
    }
}
