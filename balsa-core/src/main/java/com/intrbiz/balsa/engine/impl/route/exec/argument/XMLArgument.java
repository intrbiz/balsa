package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.error.http.BalsaBadRequest;
import com.intrbiz.balsa.error.http.BalsaInternalServerError;
import com.intrbiz.balsa.listener.BalsaRequest;

public final class XMLArgument extends ArgumentBuilder<XMLArgument>
{
    protected Class<?> type;
    
    protected String variable;
    
    public XMLArgument()
    {
        super();
    }
    
    public XMLArgument type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    public String getVariable()
    {
        return this.variable;
    }
    
    public void compile(ExecutorClass cls)
    {
        cls.addImport(JAXBContext.class.getCanonicalName());
        cls.addImport(XMLStreamWriter.class.getCanonicalName());
        cls.addImport(Marshaller.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        cls.addImport(BalsaRequest.class.getCanonicalName());
        cls.addImport(BalsaBadRequest.class.getCanonicalName());
        cls.addImport(JAXBException.class.getCanonicalName());
        cls.addImport(BalsaInternalServerError.class.getCanonicalName());
        //
        cls.addField(JAXBContext.class.getSimpleName(), "xmlInCtx");
        //
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.xmlInCtx = JAXBContext.newInstance(").append(this.type.getSimpleName()).append(".class);\r\n");
        // allocate the variable we are going to use
        this.variable = cls.allocateExecutorVariable(this.type.getSimpleName());
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.type.getSimpleName()).append(" ").append(this.variable).append(";\r\n");
        sb.append("    BalsaRequest request = context.request();\r\n");
        sb.append("    if (! request.isXml()) throw new BalsaBadRequest(\"The request Content-Type must be application/xml\");\r\n");
        sb.append("    try\r\n");
        sb.append("    {\r\n");
        sb.append("      ").append(this.variable).append(" = (").append(this.type.getSimpleName()).append(") this.xmlInCtx.createUnmarshaller().unmarshal(request.getXMLReader());\r\n");
        sb.append("    }\r\n");
        sb.append("    catch (JAXBException je)\r\n");
        sb.append("    {\r\n");
        sb.append("      throw new BalsaInternalServerError(\"Failed to decode XML\", je);\r\n");
        sb.append("    }\r\n");
    }

    public void verify(Class<?> parameterType)
    {
        if (this.type != parameterType) throw new IllegalArgumentException("Parameter argument type must match decoder class"); 
    }
    
    @Override
    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class<?> parameterType)
    {
        this.type(parameterType);
    }
}
