package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public class XMLResponse extends ResponseBuilder
{
    private Class<?> type;
    
    public XMLResponse()
    {
        super();
    }
    
    public XMLResponse type(Class<?> type)
    {
        this.type = type;
        //
        return this;
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        cls.addImport(JAXBContext.class.getCanonicalName());
        cls.addImport(XMLStreamWriter.class.getCanonicalName());
        cls.addImport(Marshaller.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        //
        cls.addField(JAXBContext.class.getSimpleName(), "xmlResCtx");
        //
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.xmlResCtx = JAXBContext.newInstance(").append(this.type.getSimpleName()).append(".class);\r\n");
        //
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // encode the response to XML\r\n");
        sb.append("    XMLStreamWriter writer = context.response().ok().xml().getXMLWriter();\r\n");
        sb.append("    Marshaller m = this.xmlResCtx.createMarshaller();\r\n");
        sb.append("    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);\r\n");
        sb.append("    m.marshal(res, writer);\r\n");
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType)
    {
        this.type(returnType);
    }
    
    @Override
    public void verify(Class<?> returnType)
    {
        if (void.class == returnType) throw new IllegalStateException("Cannot encode the response of a void method!");
    }
}
