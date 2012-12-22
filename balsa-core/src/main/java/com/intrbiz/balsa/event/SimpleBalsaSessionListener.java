package com.intrbiz.balsa.event;

/**
 * A Balsa session listener which takes events from the session engine and demultiplex them
 */
public class SimpleBalsaSessionListener implements BalsaEventListener<BalsaSessionEvent>
{
    @Override
    public final void process(BalsaSessionEvent event)
    {   
        if (event instanceof CreatedBalsaSession)
        {
            this.createdBalsaSession((CreatedBalsaSession) event);
        }
        else if (event instanceof DestroyedBalsaSession)
        {
            this.destroyedBalsaSession((DestroyedBalsaSession) event);
        }
    }
    
    public void createdBalsaSession(CreatedBalsaSession event)
    {        
    }
    
    public void destroyedBalsaSession(DestroyedBalsaSession event)
    {
    }
}
