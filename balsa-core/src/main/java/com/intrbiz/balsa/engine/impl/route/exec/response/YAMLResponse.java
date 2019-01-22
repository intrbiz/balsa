package com.intrbiz.balsa.engine.impl.route.exec.response;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.metadata.YAML;

public class YAMLResponse extends ResponseBuilder
{
    private Class<?> type;

    private HTTPStatus status = HTTPStatus.OK;

    private boolean notFoundIfNull = false;
    
    protected List<Class<?>> subTypes = new LinkedList<Class<?>>();

    public YAMLResponse()
    {
        super();
    }

    public YAMLResponse type(Class<?> type)
    {
        this.type = type;
        return this;
    }
    
    public YAMLResponse subTypes(Class<?>... subTypes)
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
        cls.addImport(YAMLFactory.class.getCanonicalName());
        cls.addImport(YAMLGenerator.class.getCanonicalName());
        cls.addImport(YAMLParser.class.getCanonicalName());
        cls.addImport(ObjectMapper.class.getCanonicalName());
        cls.addImport(JsonGenerator.class.getCanonicalName());
        cls.addImport(HTTPStatus.class.getCanonicalName());
        cls.addImport(this.type.getCanonicalName());
        cls.addImport(Iterable.class.getCanonicalName());
        if (this.notFoundIfNull) cls.addImport(BalsaNotFound.class.getCanonicalName());
        // setup the object mapper
        cls.addField(ObjectMapper.class.getSimpleName(), "yamlResMapper");
        StringBuilder csb = cls.getConstructorLogic();
        csb.append("    this.yamlResMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID).enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));\r\n");
        for (Class<?> subType : this.subTypes)
        {
            cls.addImport(subType.getCanonicalName());
            csb.append("    this.yamlResMapper.registerSubtypes(").append(subType.getSimpleName()).append(".class);\r\n");
        }
        //
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // encode the response to YAML\r\n");
        if (this.notFoundIfNull)
        {
            sb.append("    if (res == null) throw new BalsaNotFound();");
        }
        sb.append("    YAMLGenerator writer = context.response().status(HTTPStatus." + this.status.name() + ").yaml().getYamlWriter();\r\n");
        if (Iterable.class.isAssignableFrom(this.type))
        {
            sb.append("    writer.writeStartArray();\r\n");
            sb.append("    for (Object resElement : res)\r\n");
            sb.append("    {\r\n");
            sb.append("        this.yamlResMapper.writeValue(writer, resElement);\r\n");
            sb.append("    }\r\n");
            sb.append("    writer.writeEndArray();\r\n");
        }
        else
        {
            sb.append("    this.yamlResMapper.writeValue(writer, res);\r\n");
        }
    }

    @Override
    public void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType)
    {
        this.type(returnType);
        // what should the status be?
        this.status = ((YAML) a).status();
        this.notFoundIfNull = ((YAML) a).notFoundIfNull();
        this.subTypes(((YAML) a).value());
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
