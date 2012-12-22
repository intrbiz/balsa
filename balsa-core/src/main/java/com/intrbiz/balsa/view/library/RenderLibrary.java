package com.intrbiz.balsa.view.library;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;

public interface RenderLibrary
{   
    Renderer loadRenderer(Component component) throws BalsaException;
    
    String getName();
    String getLicense();
    String getDescription();
    String getInfo();
}
