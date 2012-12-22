package com.intrbiz.balsa.bean.impl;

import com.intrbiz.balsa.bean.BeanFactory;
import com.intrbiz.balsa.bean.BeanProvider;

/**
 * A non pooled bean provider
 */
public class NonPooledBean<E> implements BeanProvider<E>
{
    private final Class<E> type;
    
    private final BeanFactory<E> factory;
    
    public NonPooledBean(Class<E> type)
    {
        super();
        this.type = type;
        this.factory = new ClassFactory<E>(type);
    }
    
    public NonPooledBean(Class<E> type, BeanFactory<E> factory)
    {
        super();
        this.type = type;
        this.factory = factory;
    }

    @Override
    public Class<E> getBeanClass()
    {
        return this.type;
    }

    @Override
    public E activate()
    {
        E bean = this.factory.make();
        this.factory.activate(bean);
        return bean;
    }

    @Override
    public void deactivate(E bean)
    {
        this.factory.deactivate(bean);
    }
}
