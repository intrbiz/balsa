package com.intrbiz.balsa.engine;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaException;

public interface BalsaEngine
{
    String getEngineName();
    
    BalsaApplication getBalsaApplication();
    
    void setBalsaApplication(BalsaApplication application);
    
    public void start() throws BalsaException;

    public void stop();
}
