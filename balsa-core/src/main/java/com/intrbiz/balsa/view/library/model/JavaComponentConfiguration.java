package com.intrbiz.balsa.view.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;

@XmlType(name = "java-component")
@XmlRootElement(name = "java-component")
public class JavaComponentConfiguration extends ComponentConfiguration
{
    private static final long serialVersionUID = 1L;
    
    private String classname;

    public JavaComponentConfiguration()
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

    @SuppressWarnings("unchecked")
    public Class<? extends Component> componentClass()
    {
        try
        {
            return (Class<? extends Component>) Class.forName(this.getClassname());
        }
        catch (Exception e)
        {
        }
        return null;
    }

    @Override
    public Component load(String name, String library) throws BalsaException
    {
        try
        {
            Class<?> c = Class.forName(this.getClassname());
            Component co = (Component) c.newInstance();
            co.setName(name);
            return co;
        }
        catch (Exception e)
        {
            throw new BalsaException("Could not load component class",e);
        }
    }
}
