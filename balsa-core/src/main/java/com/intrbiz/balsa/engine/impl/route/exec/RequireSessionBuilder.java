package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;

public class RequireSessionBuilder extends SecurityBuilder
{
    public RequireSessionBuilder()
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
        sb.append("    // force a session to be created\r\n");
        sb.append("    context.session();\r\n");
    }
}
