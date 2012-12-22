package com.intrbiz.balsa.view.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;

@XmlType(name = "renderer")
@XmlRootElement(name = "renderer")
public class JavaRendererConfiguration extends RendererConfiguration
{
    private String classname;

    public JavaRendererConfiguration()
    {
        super();
    }

    @XmlAttribute(name = "classname")
    public String getClassname()
    {
        return classname;
    }

    public void setClassname(String classname)
    {
        this.classname = classname;
    }

    @Override
    public Renderer load(Component component) throws BalsaException
    {
        try
        {
            Class<?> r = Class.forName(this.getClassname());
            Renderer ro = (Renderer) r.newInstance();
            return ro;
        }
        catch (Exception e)
        {
            throw new BalsaException("Could not load renderer class", e);
        }

    }
}
