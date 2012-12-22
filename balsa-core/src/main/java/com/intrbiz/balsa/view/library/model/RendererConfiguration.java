package com.intrbiz.balsa.view.library.model;

import javax.xml.bind.annotation.XmlAttribute;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;

public abstract class RendererConfiguration
{
    private String component;
    
    @XmlAttribute(name = "component")
    public String getComponent()
    {
        return component;
    }

    public void setComponent(String component)
    {
        this.component = component;
    }
    
    public abstract Renderer load(Component component) throws BalsaException;
}
