package com.intrbiz.balsa.event;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BalsaEventDispatcher<T extends BalsaEvent> implements BalsaEventAnnouncer<T>
{
    private volatile boolean listening = false;
    
    private ConcurrentLinkedQueue<BalsaEventListener<T>> listeners = new ConcurrentLinkedQueue<BalsaEventListener<T>>();
    
    public BalsaEventDispatcher()
    {
        super();
    }
    
    public void announce(T event)
    {
        for (BalsaEventListener<T> l : this.listeners)
        {
            l.process(event);
        }
    }
    
    public final boolean isListening()
    {
        return this.listening;
    }
    
    public BalsaEventListenerRegistration listen(final BalsaEventListener<T> listener)
    {
        this.listeners.add(listener);
        this.listening = true;
        return new BalsaEventListenerRegistration(){
            @Override
            public void stop()
            {
                BalsaEventDispatcher.this.unlisten(listener);
            }
        };
    }
    
    public void unlisten(final BalsaEventListener<T> listener)
    {
        this.listeners.remove(listener);
    }
}
