package com.intrbiz.balsa.engine.impl.route.exec.security;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.engine.impl.route.exec.ExecutorClass;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequirePermissions;

public class PermissionsBuilder extends SecurityBuilder
{
    private List<String> permissions = new LinkedList<String>();

    @Override
    public void fromAnnotation(Annotation a)
    {
        if (a instanceof RequirePermissions)
        {
            RequirePermissions pr = (RequirePermissions) a;
            for (RequirePermission p : pr.value())
            {
                this.permissions.add(p.value());
            }
        }
        else if (a instanceof RequirePermissions)
        {
            RequirePermission p = (RequirePermission) a;
            this.permissions.add(p.value());
        }
    }
    
    public PermissionsBuilder permission(String permission)
    {
        this.permissions.add(permission);
        return this;
    }
    
    public PermissionsBuilder permission(String[] permissions)
    {
        for (String permission : permissions)
        {
            this.permissions.add(permission);
        }
        return this;
    }

    @Override
    public void compile(ExecutorClass cls)
    {
        StringBuilder sb = cls.getExecutorLogic();
        //
        for (String permission : this.permissions)
        {
            sb.append("    // require ").append(permission).append("\r\n");
            sb.append("    context.require(context.permission(\"").append(permission).append("\"));\r\n");
        }
    }
    
    @Override
    public boolean isSingular()
    {
        return false;
    }
}
