package com.intrbiz.balsa.bean;

/**
 * Create a bean
 */
public abstract class BeanFactory<E>
{
    public abstract E make();
    
    public void activate(E bean)
    {        
    }
    
    public void deactivate(E bean)
    {
    }
}
