package com.intrbiz.balsa.view.core.security;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;

public class AccessTokenRenderer extends GenericRenderer
{
    @Override
    public void encodeStart(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        String id = ((AccessTokenComponent) component).getId();
        //
        out.openStartTagPad("input");
        out.attribute("type", "hidden");
        out.attribute("name", id);
        out.attribute("value", context.generateAccessToken());
        out.closeStartTagLn();
        out.endTagLn("input");
    }

    @Override
    public void encodeEnd(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
    }
}
