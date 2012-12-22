package com.intrbiz.balsa.event;

import java.util.EventListener;

public interface BalsaEventListener<T extends BalsaEvent> extends EventListener
{
 
    public void process(T event);
    
}
