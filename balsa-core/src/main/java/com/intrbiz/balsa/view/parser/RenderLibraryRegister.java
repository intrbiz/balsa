package com.intrbiz.balsa.view.parser;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.library.RenderLibrary;
import com.intrbiz.balsa.view.library.XMLRenderLibrary;
import com.intrbiz.balsa.view.renderer.Renderer;

public class RenderLibraryRegister
{
    private List<RenderLibrary> libraries = new LinkedList<RenderLibrary>();

    public RenderLibraryRegister()
    {
    }

    public void loadLibrary(String name) throws BalsaException
    {
        RenderLibrary rl = XMLRenderLibrary.loadLibrary(name);
        if (!this.libraries.contains(rl)) this.libraries.add(rl);
    }

    public Renderer loadRenderer(Component comp) throws BalsaException
    {
        for (RenderLibrary lib : libraries)
        {
            Renderer r = lib.loadRenderer(comp);
            if (r != null) 
                return r;
        }
        return null;
    }

}
