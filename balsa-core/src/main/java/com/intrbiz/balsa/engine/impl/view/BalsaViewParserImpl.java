package com.intrbiz.balsa.engine.impl.view;

import java.io.IOException;
import java.io.Reader;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.engine.view.BalsaViewParser;
import com.intrbiz.balsa.engine.view.BalsaViewSource.Resource;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.component.View;
import com.intrbiz.balsa.view.parser.Parser;

public class BalsaViewParserImpl implements BalsaViewParser
{
    @Override
    public BalsaView parse(BalsaView previous, Resource resource, BalsaContext context) throws BalsaException
    {
        try
        {
            Reader xml = resource.openReader();
            //
            View view = new View(previous);
            //
            Component root = Parser.parse(context, view, xml);
            if (root == null) throw new BalsaException("Could not parse the view: " + resource.getName());
            view.setRoot(root);
            //
            return view;
        }
        catch (IOException e)
        {
            throw new BalsaException("Failed to parse view", e);
        }
    }
}
