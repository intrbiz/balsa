package com.intrbiz.balsa.view.library.model;

import javax.xml.bind.annotation.XmlAttribute;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.configuration.Configuration;

public abstract class ComponentConfiguration extends Configuration
{
    private String name;

    public ComponentConfiguration()
    {
        super();
    }

    @XmlAttribute(name = "name")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public abstract Component load(String id, String name, String library) throws BalsaException;
}
