package com.intrbiz.balsa.engine.view;

/**
 * Allow a view name to be rewritten, allowing the redirection of views internally
 */
public abstract class BalsaViewRewriteRule
{
    public abstract boolean accept(String view);
    
    public abstract String rewrite(String view);
    
    public boolean isLast()
    {
        return false;
    }
}
