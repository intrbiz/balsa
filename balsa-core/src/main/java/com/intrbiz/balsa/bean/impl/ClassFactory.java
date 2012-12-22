package com.intrbiz.balsa.bean.impl;

import java.lang.reflect.Method;

import com.intrbiz.balsa.bean.BeanFactory;

public class ClassFactory<E> extends BeanFactory<E>
{
    private static final Object[] EMPTY = {};
    private static final Class<?>[] EMPTY_PARAMETERS = {};
    
    private final Class<E> clazz;
    
    private Method activator = null;
    private Method deactivator = null;
    
    public ClassFactory(Class<E> clazz)
    {
        this.clazz = clazz;
        // look for an activate method
        try
        {
            this.activator = this.clazz.getMethod("activate", EMPTY_PARAMETERS);
        }
        catch (NoSuchMethodException e)
        {
        }
        // look for a deactivate method
        try
        {
            this.deactivator = this.clazz.getMethod("deactivate", EMPTY_PARAMETERS);
        }
        catch (NoSuchMethodException e)
        {
        }
    }
    
    public E make()
    {
        try
        {
            return this.clazz.newInstance();
        }
        catch (InstantiationException e)
        {
        }
        catch (IllegalAccessException e)
        {
        }
        return null;
    }

    @Override
    public void activate(E bean)
    {
        if (this.activator != null)
        {
            try
            {
                this.activator.invoke(bean, EMPTY);
            }
            catch (Error e)
            {
                throw e;
            }
            catch (Throwable t)
            {
            }
        }
    }

    @Override
    public void deactivate(E bean)
    {
        if (this.deactivator != null)
        {
            try
            {
                this.deactivator.invoke(bean, EMPTY);
            }
            catch (Error e)
            {
                throw e;
            }
            catch (Throwable t)
            {
            }
        }
    }
}
