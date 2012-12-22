package com.intrbiz.balsa.bean;

/**
 * A bean manager
 */
public interface BeanProvider<E>
{
    /**
     * The bean name
     * @return
     * returns String
     */
    Class<E> getBeanClass();
    
    /**
     * Create a bean
     * @return
     * returns E
     */
    E activate();
    
    /**
     * Destroy a bean
     * @param bean
     * returns void
     */
    void deactivate(E bean);
}
