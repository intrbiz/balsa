package com.intrbiz.balsa.view.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.component.View;
import com.intrbiz.balsa.view.loader.Loader;

public class ParserContext
{
    private Component root;

    private List<PostProcessor> postProcessors = new LinkedList<PostProcessor>();

    private final String idPrefix;
    
    private final View view;

    private int idCount = 0;

    private Set<String> ids = new HashSet<String>();

    private Loader loader;

    public ParserContext(String idPrefix, View view, Loader loader)
    {
        this.idPrefix = idPrefix;
        this.view = view;
        this.loader = loader;
    }
    
    public View getView()
    {
        return this.view;
    }

    public Component getRoot()
    {
        return root;
    }

    public void setRoot(Component root)
    {
        this.root = root;
    }

    /**
     * @return the postProcessors
     */
    public List<PostProcessor> getPostProcessors()
    {
        return postProcessors;
    }

    /**
     * @param postProcessors
     *            the postProcessors to set
     */
    public void setPostProcessors(List<PostProcessor> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void postProcess() throws BalsaException
    {
        Collections.sort(this.postProcessors);
        for (PostProcessor pp : this.postProcessors)
        {
            pp.postProcess(this);
        }
    }

    public String getNextComponentId()
    {
        return this.idPrefix + Integer.toHexString((++this.idCount));
    }

    public void addId(String id)
    {
        this.ids.add(id);
    }

    public boolean containsId(String id)
    {
        return this.ids.contains(id);
    }

    public Loader getLoader()
    {
        return loader;
    }
}
