package com.intrbiz.balsa.engine.impl.route.exec.response;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.metadata.JSON;

public class JSONResponse extends ResponseBuilder
{
    private Class<?> type;

    private HTTPStatus status = HTTPStatus.OK;

    private boolean notFoundIfNull = false;
    
    protected List<Class<?>> subTypes = new LinkedList<Class<?>>();

    public JSONResponse()
    {
        super();
    }

    public JSONResponse type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    public JSONResponse subTypes(Class<?>... subTypes)
    {
        for (Class<?> subType : subTypes)
        {
            this.subTypes.add(subType);
        }
        return this;
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        cls.addImport(ObjectMapper.class.getCanonicalName());
        cls.addImport(JsonGenerator.class.getCanonicalName());
        cls.addImport(HTTPStatus.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        cls.addImport(Iterable.class.getCanonicalName());
        if (this.notFoundIfNull) cls.addImport(BalsaNotFound.class.getCanonicalName());
        // setup the object mapper
        cls.addField(ObjectMapper.class.getSimpleName(), "jsonResMapper");
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.jsonResMapper = new ObjectMapper();\r\n");
        for (Class<?> subType : this.subTypes)
        {
            cls.addImport(subType.getCanonicalName());
            csb.append("    this.jsonResMapper.registerSubtypes(").append(subType.getSimpleName()).append(".class);\r\n");
        }
        //
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // encode the response to JSON\r\n");
        if (this.notFoundIfNull)
        {
            sb.append("    if (res == null) throw new BalsaNotFound();");
        }
        sb.append("    JsonGenerator writer = context.response().status(HTTPStatus." + this.status.name() + ").json().getJsonWriter();\r\n");
        if (Iterable.class.isAssignableFrom(this.type))
        {
            sb.append("    writer.writeStartArray();\r\n");
            sb.append("    for (Object resElement : res)\r\n");
            sb.append("    {\r\n");
            sb.append("        this.jsonResMapper.writeValue(writer, resElement);\r\n");
            sb.append("    }\r\n");
            sb.append("    writer.writeEndArray();\r\n");
        }
        else
        {
            sb.append("    this.jsonResMapper.writeValue(writer, res);\r\n");
        }
    }

    @Override
    public void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType)
    {
        this.type(returnType);
        // what should the status be?
        this.status = ((JSON) a).status();
        this.notFoundIfNull = ((JSON) a).notFoundIfNull();
        this.subTypes(((JSON) a).value());
    }

    public boolean isNotFoundIfNull()
    {
        return notFoundIfNull;
    }

    public void setNotFoundIfNull(boolean notFoundIfNull)
    {
        this.notFoundIfNull = notFoundIfNull;
    }

    public HTTPStatus getStatus()
    {
        return status;
    }

    public void setStatus(HTTPStatus status)
    {
        this.status = status;
    }

    public Class<?> getType()
    {
        return type;
    }

    public void setType(Class<?> type)
    {
        this.type = type;
    }

    @Override
    public void verify(Class<?> returnType)
    {
        if (void.class == returnType) throw new IllegalStateException("Cannot encode the response of a void method!");
    }
}
