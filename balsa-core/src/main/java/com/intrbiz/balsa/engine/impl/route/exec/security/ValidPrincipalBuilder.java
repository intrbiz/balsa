package com.intrbiz.balsa.engine.impl.route.exec.security;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;

public class ValidPrincipalBuilder extends SecurityBuilder
{
    @Override
    public void fromAnnotation(Annotation a)
    {
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // require a valid user\r\n");
        sb.append("    context.require(context.validPrincipal());\r\n");
    }
}
