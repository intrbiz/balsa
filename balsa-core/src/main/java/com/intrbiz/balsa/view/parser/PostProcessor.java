package com.intrbiz.balsa.view.parser;

import com.intrbiz.balsa.BalsaException;

public abstract class PostProcessor implements Comparable<PostProcessor>
{
    private final int priority;

    public PostProcessor(int priority)
    {
        this.priority = priority;
    }

    public int compareTo(PostProcessor o)
    {
        PostProcessor other = (PostProcessor) o;
        if (other.priority < this.priority)
            return 1;
        else if (other.priority == this.priority)
            return 0;
        else
            return -1;
    }

    public abstract void postProcess(ParserContext context) throws BalsaException;
}
