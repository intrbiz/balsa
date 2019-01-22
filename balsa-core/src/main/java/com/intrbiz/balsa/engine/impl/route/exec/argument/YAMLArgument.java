package com.intrbiz.balsa.engine.impl.route.exec.argument;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.error.http.BalsaBadRequest;
import com.intrbiz.balsa.error.http.BalsaInternalServerError;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.metadata.YAML;

public final class YAMLArgument extends ArgumentBuilder<YAMLArgument>
{
    protected Class<?> type;
    
    protected String variable;
    
    protected List<Class<?>> subTypes = new LinkedList<Class<?>>();
    
    public YAMLArgument()
    {
        super();
    }
    
    public YAMLArgument type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    public YAMLArgument subTypes(Class<?>... subTypes)
    {
        for (Class<?> subType : subTypes)
        {
            this.subTypes.add(subType);
        }
        return this;
    }
    
    public String getVariable()
    {
        return this.variable;
    }
    
    public void compile(ExecutorClass cls)
    {
        cls.addImport(YAMLFactory.class.getCanonicalName());
        cls.addImport(YAMLGenerator.class.getCanonicalName());
        cls.addImport(YAMLParser.class.getCanonicalName());
        cls.addImport(ObjectMapper.class.getCanonicalName());
        cls.addImport(BalsaRequest.class.getCanonicalName());
        cls.addImport(BalsaBadRequest.class.getCanonicalName());
        cls.addImport(BalsaInternalServerError.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        // setup the object mapper
        cls.addField(ObjectMapper.class.getSimpleName(), "yamlInCtx");
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.yamlInCtx = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID).enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));\r\n");
        for (Class<?> subType : this.subTypes)
        {
            cls.addImport(subType.getCanonicalName());
            csb.append("    this.yamlInCtx.registerSubtypes(").append(subType.getSimpleName()).append(".class);\r\n");
        }
        // allocate the variable we are going to use
        this.variable = cls.allocateExecutorVariable(this.type.getSimpleName(), "model");
        // write the code
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // bind parameter ").append(this.index).append("\r\n");
        sb.append("    ").append(this.type.getSimpleName()).append(" ").append(this.variable).append(";\r\n");
        sb.append("    BalsaRequest request = context.request();\r\n");
        sb.append("    if (! request.isYaml()) throw new BalsaBadRequest(\"The request Content-Type must be text/yaml\");\r\n");
        sb.append("    try\r\n");
        sb.append("    {\r\n");
        sb.append("        ").append(this.variable).append(" = this.yamlInCtx.readValue(request.getYamlReader(), ").append(this.type.getSimpleName()).append(".class);\r\n");
        sb.append("    }\r\n");
        sb.append("    catch (Exception ye)\r\n");
        sb.append("    {\r\n");
        sb.append("      throw new BalsaInternalServerError(\"Failed to decode YAML\", ye);\r\n");
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
        this.subTypes(((YAML) a).value());
    }
}
