package com.intrbiz.balsa.engine.impl.route.exec.response;

import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.metadata.JSON;

public class JSONResponse extends ResponseBuilder
{
    private Class<?> type;

    private HTTPStatus status = HTTPStatus.OK;

    private boolean notFoundIfNull = false;

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
        cls.addImport(HTTPStatus.class.getCanonicalName());
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
        sb.append("    JsonGenerator writer = context.response().status(" + (this.notFoundIfNull ? "res == null ? HTTPStatus.NotFound : " : "") + "HTTPStatus." + this.status.name() + ").json().getJsonWriter();\r\n");
        sb.append("    this.jsonResMapper.writeValue(writer, res);\r\n");
    }

    @Override
    public void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType)
    {
        this.type(returnType);
        // what should the status be?
        this.status = ((JSON) a).status();
        this.notFoundIfNull = ((JSON) a).notFoundIfNull();
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
