package com.intrbiz.balsa.view.library;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.library.model.ComponentConfiguration;
import com.intrbiz.balsa.view.library.model.JavaComponentConfiguration;
import com.intrbiz.balsa.view.library.model.PostProcessorConfiguration;
import com.intrbiz.balsa.view.parser.PostProcessor;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "component-library")
@XmlRootElement(name = "component-library")
public class XMLComponentLibrary extends Configuration implements ComponentLibrary
{
    private List<ComponentConfiguration> components = new LinkedList<ComponentConfiguration>();

    private List<PostProcessorConfiguration> postProcessors = new LinkedList<PostProcessorConfiguration>();

    private String license;

    private String description;

    private String info;

    public XMLComponentLibrary()
    {
        super();
    }

    @XmlAttribute(name = "license")
    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    @XmlElement(name = "description")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @XmlElement(name = "info")
    public String getInfo()
    {
        return info;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }

    @XmlElementRefs({ @XmlElementRef(type = JavaComponentConfiguration.class) })
    public List<ComponentConfiguration> getComponents()
    {
        return components;
    }

    public void setComponents(List<ComponentConfiguration> components)
    {
        this.components = components;
    }

    @XmlElementRef(type = PostProcessorConfiguration.class)
    public List<PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
    }


    public static XMLComponentLibrary read(InputStream in) throws JAXBException
    {
        return (XMLComponentLibrary) Configuration.read(XMLComponentLibrary.class, in);
    }

    public static XMLComponentLibrary loadLibrary(String name) throws BalsaException
    {
        try
        {
            // Convert the url into the library xml path
            String clPath = name.replace(".", "/");
            // load the hybrid library from the class path resource
            InputStream res = XMLComponentLibrary.class.getResourceAsStream("/" + clPath + "/component-library.xml");
            if (res == null) throw new BalsaException("Could not resolve component library: " + name);
            XMLComponentLibrary cl = XMLComponentLibrary.read(res);
            return cl;
        }
        catch (JAXBException e)
        {
            throw new BalsaException("Could not load component library: " + name, e);
        }
    }

    public static void write(XMLComponentLibrary obj, OutputStream out) throws JAXBException
    {
        Configuration.write(XMLComponentLibrary.class, obj, out);
    }

    @Override
    public Object create() throws Exception
    {
        throw new BalsaException("You make no sense!");
    }

    @Override
    public Class<? extends Component> componentClass(String name)
    {
        for (ComponentConfiguration cf : this.components)
        {
            if (name.equals(cf.getName()))
            {
                if (cf instanceof JavaComponentConfiguration) { return ((JavaComponentConfiguration) cf).componentClass(); }
                break;
            }
        }
        return null;
    }

    @Override
    public Set<String> componentNames()
    {
        Set<String> names = new TreeSet<String>();
        for (ComponentConfiguration cf : this.components)
        {
            names.add(cf.getName());
        }
        return names;
    }

    @Override
    public Component load(String name) throws BalsaException
    {
        ComponentConfiguration fact = null;
        for (ComponentConfiguration cf : this.components)
        {
            if (name.equalsIgnoreCase(cf.getName()))
            {
                fact = cf;
                break;
            }
        }
        if (fact == null)
        {
            for (ComponentConfiguration cf : this.components)
            {
                if ("*".equalsIgnoreCase(cf.getName()))
                {
                    fact = cf;
                    break;
                }
            }
        }
        if (fact != null)
        {
            try
            {
                Component c = (Component) fact.load(name, this.getName());
                return c;
            }
            catch (BalsaException e)
            {
                throw new BalsaException("Could not load component: " + name, e);
            }
        }
        throw new BalsaException("Unkown component: " + name + " in " + this.getName());
    }

    @Override
    public List<PostProcessor> postprocessors()
    {
        List<PostProcessor> r = new LinkedList<PostProcessor>();
        for (PostProcessorConfiguration pp : this.postProcessors)
        {
            try
            {
                r.add((PostProcessor) pp.create());
            }
            catch (Exception e)
            {
            }
        }
        return r;
    }
}
