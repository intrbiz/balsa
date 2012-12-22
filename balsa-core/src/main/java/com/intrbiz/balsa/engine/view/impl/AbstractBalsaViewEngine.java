package com.intrbiz.balsa.engine.view.impl;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.ViewEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.view.component.View;
import com.intrbiz.balsa.view.loader.Loader;
import com.intrbiz.cache.IBCache;
import com.intrbiz.cache.WeakCache;

public abstract class AbstractBalsaViewEngine extends AbstractBalsaEngine implements ViewEngine
{
    protected Loader viewLoader;

    private IBCache<String, View> cache = new WeakCache<String, View>();

    private Logger logger = Logger.getLogger(AbstractBalsaViewEngine.class);

    private boolean cacheOn = true;

    private String[] templates = new String[0];

    public AbstractBalsaViewEngine(Loader viewLoader)
    {
        super();
        this.viewLoader = viewLoader;
    }

    public Loader getViewLoader()
    {
        return viewLoader;
    }

    @Override
    public BalsaView load(String[] names, boolean useTemplate, BalsaContext context) throws BalsaException
    {
        if (names == null || names.length == 0) throw new BalsaException("Cannot load view, no names given");
        String key = key(names);
        View head = this.cacheOn ? this.cache.get(key) : null;
        if (head != null) return head;
        //
        View tail = null;
        if (useTemplate && this.templates != null)
        {
            for (String name : this.templates)
            {
                this.logger.trace("Parsing template view: " + name);
                tail = this.getViewLoader().load(tail, name, context);
                if (head == null) head = tail;
            }
        }
        for (String name : names)
        {
            this.logger.trace("Parsing view: " + name);
            tail = this.getViewLoader().load(tail, name, context);
            if (head == null) head = tail;
        }
        if (this.cacheOn) this.cache.cachePrivate(key, head);
        return head;
    }

    private static String key(String[] names)
    {
        StringBuilder sb = new StringBuilder("|");
        for (String name : names)
        {
            sb.append(name).append("|");
        }
        return sb.toString();
    }

    @Override
    public boolean isCache()
    {
        return this.cacheOn;
    }

    @Override
    public void cacheOn()
    {
        this.cacheOn = true;
    }

    @Override
    public void cacheOff()
    {
        this.cacheOn = false;
        this.cache.clear();
        this.logger.warn("View caching has been disabled");
    }

    @Override
    public String[] getTemplates()
    {
        return this.templates;
    }

    @Override
    public void template(String template)
    {
        if (this.templates == null || this.templates.length == 0)
        {
            this.templates = new String[] { template };
        }
        else
        {
            this.templates = Arrays.copyOf(this.templates, this.templates.length);
            this.templates[this.templates.length - 1] = template;
        }
    }

    @Override
    public void templates(String[] templates)
    {
        if (templates != null)
            this.templates = templates;
        else
            this.templates = new String[0];
    }
}
