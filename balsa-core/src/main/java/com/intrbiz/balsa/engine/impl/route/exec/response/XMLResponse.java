package com.intrbiz.balsa.engine.impl.route.exec.response;

import java.lang.annotation.Annotation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.metadata.XML;

public class XMLResponse extends ResponseBuilder
{
    private Class<?> type;

    private HTTPStatus status = HTTPStatus.OK;
    
    private boolean notFoundIfNull = false;

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
        cls.addImport(HTTPStatus.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        //
        cls.addField(JAXBContext.class.getSimpleName(), "xmlResCtx");
        //
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.xmlResCtx = JAXBContext.newInstance(").append(this.type.getSimpleName()).append(".class);\r\n");
        //
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // encode the response to XML\r\n");
        sb.append("    XMLStreamWriter writer = context.response().status(" + (this.notFoundIfNull ? "res == null ? HTTPStatus.NotFound : " : "") + "HTTPStatus." + this.status.name() + ").xml().getXMLWriter();\r\n");
        sb.append("    Marshaller m = this.xmlResCtx.createMarshaller();\r\n");
        sb.append("    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);\r\n");
        sb.append("    m.marshal(res, writer);\r\n");
    }

    @Override
    public void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType)
    {
        this.type(returnType);
        this.status = ((XML) a).status();
        this.notFoundIfNull = ((XML) a).notFoundIfNull();
    }

    public Class<?> getType()
    {
        return type;
    }

    public void setType(Class<?> type)
    {
        this.type = type;
    }

    public HTTPStatus getStatus()
    {
        return status;
    }

    public void setStatus(HTTPStatus status)
    {
        this.status = status;
    }

    @Override
    public void verify(Class<?> returnType)
    {
        if (void.class == returnType) throw new IllegalStateException("Cannot encode the response of a void method!");
    }
}
