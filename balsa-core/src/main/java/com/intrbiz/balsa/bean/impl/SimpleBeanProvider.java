package com.intrbiz.balsa.bean.impl;

import com.intrbiz.balsa.bean.BeanProvider;

public class SimpleBeanProvider<E> implements BeanProvider<E>
{
    private final Class<E> clazz;

    public SimpleBeanProvider(Class<E> clazz)
    {
        this.clazz = clazz;
    }

    @Override
    public Class<E> getBeanClass()
    {
        return clazz;
    }

    public E create()
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
    public void destroy(E bean)
    {
    }
}
