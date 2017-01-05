package com.intrbiz.balsa.engine.impl.route.exec.security;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;

public class AuthenticatingBuilder extends SecurityBuilder
{    
    public AuthenticatingBuilder()
    {
        super();
    }
    
    @Override
    public void fromAnnotation(Annotation a)
    {
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // require that we are currently in the process of authenticating a principal\r\n");
        sb.append("    context.require(context.authenticating());\r\n");
    }
}
