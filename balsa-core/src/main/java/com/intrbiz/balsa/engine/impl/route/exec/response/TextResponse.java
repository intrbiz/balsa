package com.intrbiz.balsa.engine.impl.route.exec.response;

import java.lang.annotation.Annotation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.metadata.CSS;
import com.intrbiz.metadata.HTML;
import com.intrbiz.metadata.JavaScript;
import com.intrbiz.metadata.Text;
import com.intrbiz.util.compiler.util.JavaUtil;

public class TextResponse extends ResponseBuilder
{
    private Class<?> type;

    private HTTPStatus status = HTTPStatus.OK;

    private String contentType = "text/plain";

    public TextResponse()
    {
        super();
    }

    public TextResponse type(Class<?> type)
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
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // write out the text response\r\n");
        sb.append("    context.response().status(HTTPStatus." + this.status.name() + ").contentType(\"").append(JavaUtil.escapeString(this.contentType)).append("\").write(res);\r\n");
    }

    @Override
    public void fromAnnotation(Annotation a, Annotation[] annotations, Class<?> returnType)
    {
        this.type(returnType);
        if (a instanceof Text)
        {
            this.status = ((Text) a).status();
            this.contentType = ((Text) a).contentType();
        }
        else if (a instanceof HTML)
        {
            this.status = ((HTML) a).status();
            this.contentType = "text/html";
        }
        else if (a instanceof CSS)
        {
            this.status = ((CSS) a).status();
            this.contentType = "text/css";
        }
        else if (a instanceof JavaScript)
        {
            this.status = ((JavaScript) a).status();
            this.contentType = "text/javascript";
        }
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

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    @Override
    public void verify(Class<?> returnType)
    {
        if (String.class != returnType) throw new IllegalStateException("The route does not return a String!");
    }
}
