package com.intrbiz.balsa.view.parser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.view.component.Component;

public class ParserContext
{
    private Component root;

    private List<PostProcessor> postProcessors = new LinkedList<PostProcessor>();
    
    private final BalsaView view;

    public ParserContext(BalsaView view)
    {
        this.view = view;
    }
    
    public BalsaView getView()
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
}
