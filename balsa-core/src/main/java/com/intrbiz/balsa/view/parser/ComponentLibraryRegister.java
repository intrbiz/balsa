package com.intrbiz.balsa.view.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.library.ComponentLibrary;
import com.intrbiz.balsa.view.library.XMLComponentLibrary;

public class ComponentLibraryRegister
{
    private Map<String, ComponentLibrary> libraries = new HashMap<String, ComponentLibrary>();

    private ComponentLibrary defaultLibrary;

    private List<PostProcessor> requiredPostProcessors = new LinkedList<PostProcessor>();

    public ComponentLibraryRegister()
    {
    }

    private ComponentLibrary readLibrary(String url) throws BalsaException
    {
        try
        {
            return XMLComponentLibrary.loadLibrary(url);
        }
        catch (BalsaException je)
        {
            throw je;
        }
        catch (Exception e)
        {
            throw new BalsaException("Cannot load component library: " + url, e);
        }
    }

    public void loadLibrary(String url) throws BalsaException
    {
        if (!this.libraries.containsKey(url))
        {
            ComponentLibrary cl = this.readLibrary(url);
            registerLibrary(url, cl);
        }
    }
    
    public void loadDefaultLibrary(String url) throws BalsaException
    {
        if (!this.libraries.containsKey(url))
        {
            ComponentLibrary cl = this.readLibrary(url);
            registerLibrary(url, cl);
            this.defaultLibrary = cl;
        }
    }

    public void registerLibrary(String url, ComponentLibrary cl)
    {
        for (PostProcessor pp : cl.postprocessors())
        {
            boolean stored = false;
            for (PostProcessor rpp : this.requiredPostProcessors)
            {
                if (rpp.getClass().equals(pp.getClass()))
                {
                    stored = true;
                    break;
                }
            }
            if (!stored) this.requiredPostProcessors.add(pp);
        }
        this.libraries.put(url, cl);
    }

    public Component loadComponent(String library, String name, String id) throws BalsaException
    {
        ComponentLibrary lib = this.libraries.get(library);
        if (lib == null) lib = this.defaultLibrary;
        if (lib == null) throw new BalsaException("Cannot find library");
        return lib.load(name, id);
    }

    public List<PostProcessor> getRequiredPostProcessors()
    {
        return requiredPostProcessors;
    }
}
