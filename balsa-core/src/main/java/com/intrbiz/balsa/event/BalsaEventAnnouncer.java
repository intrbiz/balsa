package com.intrbiz.balsa.event;

public interface BalsaEventAnnouncer<T extends BalsaEvent>
{
    BalsaEventListenerRegistration listen(final BalsaEventListener<T> listener);
    
    void unlisten(final BalsaEventListener<T> listener);
}
