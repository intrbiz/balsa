package com.intrbiz.balsa;

import java.io.IOException;
import java.io.Reader;

import org.parboiled.Parboiled;
import org.pegdown.Extensions;
import org.pegdown.Parser;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;

import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.engine.view.BalsaViewParser;
import com.intrbiz.balsa.engine.view.BalsaViewSource.Resource;
import com.intrbiz.balsa.pegdown.ToBalsaVisitor;
import com.intrbiz.balsa.view.component.View;

public class PegdownViewParser implements BalsaViewParser
{
    @Override
    public BalsaView parse(BalsaView previous, Resource resource, BalsaContext context) throws BalsaException
    {
        try
        {
            View view = this.createView(previous, resource, context);
            // parse
            Parser parser = Parboiled.createParser(Parser.class, Extensions.TABLES | Extensions.AUTOLINKS, 2000L, Parser.DefaultParseRunnerProvider, PegDownPlugins.NONE);
            RootNode root = parser.parse(slurp(resource.openReader()).toCharArray());
            // visit
            ToBalsaVisitor visitor = new ToBalsaVisitor(view);
            visitor.startDocument();
            root.accept(visitor);
            visitor.endDocument();
            view.setRoot(visitor.getRoot());
            // return the view
            return view;
        }
        catch (IOException e)
        {
            throw new BalsaException("Failed to parse view: " + resource.getName(), e);
        }
    }

    protected View createView(BalsaView previous, Resource resource, BalsaContext context)
    {
        return new View(previous);
    }

    private String slurp(Reader reader) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            char[] b = new char[4096];
            int r;
            while ((r = reader.read(b)) != -1)
            {
                sb.append(b, 0, r);
            }
        }
        finally
        {
            reader.close();
        }
        return sb.toString();
    }
}
