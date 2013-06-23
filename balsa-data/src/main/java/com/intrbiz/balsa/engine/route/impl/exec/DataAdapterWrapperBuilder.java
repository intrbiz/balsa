package com.intrbiz.balsa.engine.route.impl.exec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.engine.impl.route.exec.ArgumentBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.RouteWrapperBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.balsa.metadata.WithDataAdapters;

public class DataAdapterWrapperBuilder extends RouteWrapperBuilder
{
    private List<WithDataAdapter> adapters = new LinkedList<WithDataAdapter>();

    @Override
    public void fromAnnotation(Annotation a)
    {
        if (a instanceof WithDataAdapters)
        {
            for (WithDataAdapter wda : ((WithDataAdapters) a).value())
            {
                this.fromAnnotation(wda);
            }
        }
        else if (a instanceof WithDataAdapter)
        {
            this.fromAnnotation((WithDataAdapter) a);
        }
    }

    private void fromAnnotation(WithDataAdapter wda)
    {
        this.adapters.add(wda);
    }

    @Override
    public void compileBefore(ExecutorClass cls)
    {
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // connect data adapters\r\n");
        int i = 0;
        for (final WithDataAdapter wda : this.adapters)
        {
            cls.addImport(wda.value().getCanonicalName());
            sb.append("    // connect ").append(wda.value().getSimpleName()).append("\r\n");
            sb.append("    try(").append(wda.value().getSimpleName()).append(" adapter").append(i).append(" = ").append(wda.value().getSimpleName()).append(".connect(");
            if (wda.server() != null && (!"".equals(wda.server())))
            {
                sb.append("\"").append(wda.server()).append("\"");
            }
            sb.append("))\r\n");
            sb.append("    {\r\n");
            i++;
        }
    }

    @Override
    @SuppressWarnings("unused")
    public void compileAfter(ExecutorClass cls)
    {
        StringBuilder sb = cls.getExecutorLogic();
        sb.append("    // end data adapters\r\n");
        for (WithDataAdapter wda : this.adapters)
        {
            sb.append("    }\r\n");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ArgumentBuilder<?> argument(Method method, int index, Class<?> arguementType, Annotation[] annotations)
    {
        int i = 0;
        for (final WithDataAdapter wda : this.adapters)
        {
            if (arguementType == wda.value())
            {
                final int adapter = i;
                return new ArgumentBuilder()
                {
                    @Override
                    public void fromAnnotation(Annotation a, Annotation[] parameterAnnotations, Class parameterType)
                    {
                    }

                    @Override
                    public void compile(ExecutorClass cls)
                    {
                        StringBuilder sb = cls.getExecutorLogic();
                        sb.append("    // bind parameter ").append(this.index).append("\r\n");
                        sb.append("    ").append(this.parameterType.getSimpleName()).append(" p").append(this.index).append(" = adapter").append(adapter).append(";\r\n");
                    }

                    @Override
                    public void verify(Class parameterType)
                    {
                    }
                };
            }
            i++;
        }
        return null;
    }
}
