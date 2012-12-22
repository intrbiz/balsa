package com.intrbiz.balsa.bean.impl;

import com.intrbiz.balsa.bean.BeanFactory;
import com.intrbiz.balsa.bean.BeanPool;
import com.intrbiz.balsa.bean.BeanProvider;

/**
 * A pooled bean provider
 */
public class PooledBean<E> implements BeanProvider<E>
{
    private final Class<E> type;
    
    private final BeanPool<E> pool;
    
    public PooledBean(Class<E> type)
    {
        super();
        this.type = type;
        this.pool = new BeanPool<E>(new ClassFactory<E>(type));
    }
    
    public PooledBean(Class<E> type, BeanFactory<E> factory)
    {
        super();
        this.type = type;
        this.pool = new BeanPool<E>(factory);
    }

    @Override
    public Class<E> getBeanClass()
    {
        return this.type;
    }

    @Override
    public E activate()
    {
        return pool.activate();
    }

    @Override
    public void deactivate(E bean)
    {
        this.pool.deactivate(bean);
    }
}
