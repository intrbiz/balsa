package com.intrbiz.balsa.engine.impl.route.exec.wrapper;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.metadata.Status;

public class StatusWrapperBuilder extends RouteWrapperBuilder
{
    private HTTPStatus status = HTTPStatus.OK;

    @Override
    public void fromAnnotation(Annotation a)
    {
        if (a instanceof Status)
        {
            this.status = ((Status) a).value();
        }
    }

    @Override
    public void compileBefore(ExecutorClass cls)
    {
        cls.addImport(HTTPStatus.class.getCanonicalName());
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // connect data adapters\r\n");
        sb.append("    context.response().status(HTTPStatus." + this.status.name() + ");\r\n");
    }

    @Override
    public void compileAfter(ExecutorClass cls)
    {
    }
}
