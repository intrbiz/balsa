package com.intrbiz.balsa.view.core.html;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;


public class CodeRenderer extends PreRenderer
{
    @Override
    public void encodeEnd(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        out.endTag(component.getName());
    }
}
