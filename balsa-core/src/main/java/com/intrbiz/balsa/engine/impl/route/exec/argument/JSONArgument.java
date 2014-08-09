package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.error.http.BalsaBadRequest;
import com.intrbiz.balsa.error.http.BalsaInternalServerError;
import com.intrbiz.balsa.listener.BalsaRequest;

public final class JSONArgument extends ArgumentBuilder<JSONArgument>
{
    protected Class<?> type;
    
    protected String variable;
    
    public JSONArgument()
    {
        super();
    }
    
    public JSONArgument type(Class<?> type)
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
        cls.addImport(ObjectMapper.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        cls.addImport(BalsaRequest.class.getCanonicalName());
        cls.addImport(BalsaBadRequest.class.getCanonicalName());
        cls.addImport(BalsaInternalServerError.class.getCanonicalName());
        //
        cls.addField(ObjectMapper.class.getSimpleName(), "jsonInCtx");
        //
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.jsonInCtx = new ObjectMapper();\r\n");
        // allocate the variable we are going to use
        this.variable = cls.allocateExecutorVariable(this.type.getSimpleName(), "model");
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.type.getSimpleName()).append(" ").append(this.variable).append(";\r\n");
        sb.append("    BalsaRequest request = context.request();\r\n");
        sb.append("    if (! request.isJson()) throw new BalsaBadRequest(\"The request Content-Type must be application/json\");\r\n");
        sb.append("    try\r\n");
        sb.append("    {\r\n");
        sb.append("        ").append(this.variable).append(" = this.jsonInCtx.readValue(request.getJsonReader(), ").append(this.type.getSimpleName()).append(".class);\r\n");
        sb.append("    }\r\n");
        sb.append("    catch (Exception je)\r\n");
        sb.append("    {\r\n");
        sb.append("      throw new BalsaInternalServerError(\"Failed to decode JSON\", je);\r\n");
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
