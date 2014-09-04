package com.intrbiz.balsa.engine.impl.route.exec.security;

import java.lang.annotation.Annotation;

import com.intrbiz.balsa.engine.SecurityEngine.ValidationLevel;
import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.metadata.RequirePrincipal;
import com.intrbiz.metadata.RequireValidPrincipal;

public class ValidPrincipalBuilder extends SecurityBuilder
{
    private ValidationLevel validationLevel = ValidationLevel.STRONG;
    
    public ValidPrincipalBuilder()
    {
        super();
    }
    
    @Override
    public void fromAnnotation(Annotation a)
    {
        if (a instanceof RequireValidPrincipal)
        {
            this.validationLevel = ValidationLevel.STRONG;
        }
        else if (a instanceof RequirePrincipal)
        {
            this.validationLevel = ValidationLevel.WEAK;
        }
    }
    
    public ValidPrincipalBuilder(ValidationLevel validationLevel)
    {
        this.validationLevel = validationLevel;
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        cls.addImport(ValidationLevel.class.getCanonicalName());
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // require a valid user\r\n");
        sb.append("    context.require(context.validPrincipal(ValidationLevel." + this.validationLevel.toString() + "));\r\n");
    }
}
