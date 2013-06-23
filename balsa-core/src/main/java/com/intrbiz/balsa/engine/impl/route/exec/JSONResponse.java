package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public class JSONResponse extends ResponseBuilder
{
    private Class<?> type;
    
    public JSONResponse()
    {
        super();
    }
    
    public JSONResponse type(Class<?> type)
    {
        this.type = type;
        return this;
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        cls.addImport(ObjectMapper.class.getCanonicalName());
        cls.addImport(JsonGenerator.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        //
        cls.addField(ObjectMapper.class.getSimpleName(), "jsonResMapper");
        //
        // TODO thread safe?
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.jsonResMapper = new ObjectMapper();\r\n");
        //
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // encode the response to JSON\r\n");
        sb.append("    JsonGenerator writer = context.response().ok().json().getJsonWriter();\r\n");
        sb.append("    this.jsonResMapper.writeValue(writer, res);\r\n");
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
