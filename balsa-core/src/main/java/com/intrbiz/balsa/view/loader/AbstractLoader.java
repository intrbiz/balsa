package com.intrbiz.balsa.view.loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.component.View;
import com.intrbiz.balsa.view.parser.Parser;

public abstract class AbstractLoader implements Loader
{

    @Override
    public View load(View previous, String name, BalsaContext context) throws BalsaException
    {
        try
        {
            Reader xml = this.read(name);
            //
            View view = new View(previous);
            //long s = System.currentTimeMillis();
            Component root = Parser.parse(context, this, "", view, xml);
            //long e = System.currentTimeMillis();
            if (root == null) throw new BalsaException("Could not load the view: " + name);
            //System.out.println("Parsed (" + (e-s) + "ms): " + root);
            view.setRoot(root);
            return view;
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to load view: " + name, e);
        }
    }

    protected abstract Reader read(String name) throws IOException, FileNotFoundException, BalsaException;

}
