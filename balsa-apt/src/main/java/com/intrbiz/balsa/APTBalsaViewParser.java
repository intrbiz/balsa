package com.intrbiz.balsa;

import java.io.IOException;

import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.parser.ParseException;

import com.intrbiz.balsa.doxia.BalsaSink;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.engine.view.BalsaViewParser;
import com.intrbiz.balsa.engine.view.BalsaViewSource.Resource;
import com.intrbiz.balsa.view.component.View;

public class APTBalsaViewParser implements BalsaViewParser
{
    @Override
    public BalsaView parse(BalsaView previous, Resource resource, BalsaContext context) throws BalsaException
    {
        try
        {
            View view = this.createView(previous, resource, context);
            BalsaSink sink = new BalsaSink(view);
            new AptParser().parse(resource.openReader(), sink);
            view.setRoot(sink.getRoot());
            return view;
        }
        catch (IOException e)
        {
            throw new BalsaException("Failed to parse view: " + resource.getName(), e);
        }
        catch (ParseException e)
        {
            throw new BalsaException("Failed to parse view: " + resource.getName(), e);
        }
    }
    
    protected View createView(BalsaView previous, Resource resource, BalsaContext context)
    {
        return new View(previous);
    }
}
