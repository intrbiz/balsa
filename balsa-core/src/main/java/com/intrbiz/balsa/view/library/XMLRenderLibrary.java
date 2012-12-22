package com.intrbiz.balsa.view.library;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.library.model.JavaRendererConfiguration;
import com.intrbiz.balsa.view.library.model.RendererConfiguration;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "render-library")
@XmlRootElement(name = "render-library")
public class XMLRenderLibrary extends Configuration implements RenderLibrary
{
    private String info;

    private String license;

    private String description;

    private List<RendererConfiguration> renderers = new LinkedList<RendererConfiguration>();

    @XmlElementRef(type = JavaRendererConfiguration.class)
    public List<RendererConfiguration> getRenderers()
    {
        return renderers;
    }

    public void setRenderers(List<RendererConfiguration> renderers)
    {
        this.renderers = renderers;
    }

    @XmlElement(name="info")
    public String getInfo()
    {
        return info;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }

    @XmlAttribute(name="license")
    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    @XmlElement(name="description")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public Renderer loadRenderer(Component component) throws BalsaException
    {
        if (component == null) return null;
        for (RendererConfiguration rf : this.renderers)
        {
            if (component.getClass().getName().equals( rf.getComponent() ))
            {
                return rf.load(component);
            }
        }
        return null;
    }
    
    public static XMLRenderLibrary read(InputStream in) throws JAXBException
    {
        return (XMLRenderLibrary) Configuration.read(XMLRenderLibrary.class, in);
    }

    public static XMLRenderLibrary loadLibrary(String name) throws BalsaException
    {
        try
        {
            // Convert the url into the library xml path
            String clPath = name.replace(".", "/");
            // load the hybrid library from the class path resource
            // TODO
            InputStream res = XMLRenderLibrary.class.getResourceAsStream("/" + clPath + "/render-library.xml");
            if (res == null) throw new BalsaException("Could not resolve render library: " + name);
            XMLRenderLibrary rl = XMLRenderLibrary.read(res);
            return rl;
        }
        catch (JAXBException e)
        {
            throw new BalsaException("Could not load render library: " + name, e);
        }
    }

    public static void write(XMLRenderLibrary obj, OutputStream out) throws JAXBException
    {
        Configuration.write(XMLRenderLibrary.class, obj, out);
    }
}
