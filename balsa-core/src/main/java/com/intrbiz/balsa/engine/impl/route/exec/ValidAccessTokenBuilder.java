package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;
import com.intrbiz.balsa.error.security.BalsaInvalidRequest;
import com.intrbiz.balsa.util.Util;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidAccessToken;

public class ValidAccessTokenBuilder extends SecurityBuilder
{
    private enum VerifyMethod {
        SIMPLE, PATH
    };

    private VerifyMethod method = VerifyMethod.SIMPLE;

    private String parameter;

    public ValidAccessTokenBuilder()
    {
        super();
    }

    public ValidAccessTokenBuilder(String name)
    {
        this.parameter = name;
    }

    @Override
    public void fromAnnotation(Annotation a)
    {
        if (a instanceof RequireValidAccessToken)
        {
            RequireValidAccessToken vr = (RequireValidAccessToken) a;
            this.method = VerifyMethod.SIMPLE;
            this.parameter = vr.value().value();
        }
        else if (a instanceof RequireValidAccessTokenForURL)
        {
            RequireValidAccessTokenForURL vr = (RequireValidAccessTokenForURL) a;
            this.method = VerifyMethod.PATH;
            this.parameter = vr.value().value();
        }
    }

    public ValidAccessTokenBuilder path()
    {
        this.method = VerifyMethod.PATH;
        return this;
    }

    public ValidAccessTokenBuilder parameter(String name)
    {
        this.parameter = name;
        return this;
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        cls.addImport(Util.class.getCanonicalName());
        cls.addImport(BalsaInvalidRequest.class.getCanonicalName());
        //
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // verify the request is valid\r\n");
        sb.append("    String requestToken = context.param(\"").append(this.parameter).append("\");\r\n");
        if (VerifyMethod.SIMPLE.equals(this.method))
        {
            sb.append("    context.require(context.validAccessToken(requestToken), new BalsaInvalidRequest());\r\n");
        }
        else if (VerifyMethod.PATH.equals(this.method))
        {
            sb.append("    context.require(context.validAccessTokenForURL(requestToken), new BalsaInvalidRequest());\r\n");
        }
    }
}
