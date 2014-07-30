package com.intrbiz.balsa.view.library.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name="post-processor")
@XmlRootElement(name="post-processor")
public class PostProcessorConfiguration extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    public PostProcessorConfiguration()
    {
        super();
    }
}
