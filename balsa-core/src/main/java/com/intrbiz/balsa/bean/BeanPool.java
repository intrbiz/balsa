package com.intrbiz.balsa.bean;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;

@SuppressWarnings("unchecked")
public class BeanPool<E>
{
    private ObjectPool pool;
    
    private BeanFactory<E> factory;
    
    public BeanPool(BeanFactory<E> factory)
    {
        super();
        this.factory = factory;
        this.pool = new SoftReferenceObjectPool(new BasePoolableObjectFactory()
        {
            @Override
            public Object makeObject() throws Exception
            {
                return BeanPool.this.factory.make();
            }

            @Override
            public void activateObject(Object obj) throws Exception
            {
                BeanPool.this.factory.activate((E)obj);
            }

            @Override
            public void destroyObject(Object obj) throws Exception
            {
                BeanPool.this.factory.deactivate((E)obj);
            }

            @Override
            public void passivateObject(Object obj) throws Exception
            {
                BeanPool.this.factory.deactivate((E)obj);
            }

        });
    }
    
    public E activate()
    {
        try
        {
            return (E) this.pool.borrowObject();
        }
        catch (Exception e)
        {
        }
        return null;
    }

    public void deactivate(E obj)
    {
        try
        {
            this.pool.returnObject(obj);
        }
        catch (Exception e)
        {
        }
    }

    public void clear()
    {
        try
        {
            pool.clear();
        }
        catch (Exception e)
        {
        }
    }

    public void close()
    {
        try
        {
            pool.close();
        }
        catch (Exception e)
        {
        }
    }
    
    public int getNumActive()
    {
        return pool.getNumActive();
    }

    public int getNumIdle()
    {
        return pool.getNumIdle();
    }
}
