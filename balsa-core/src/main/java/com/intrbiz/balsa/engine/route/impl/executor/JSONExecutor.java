package com.intrbiz.balsa.engine.route.impl.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.route.RouteExecutor;
import com.intrbiz.balsa.engine.route.RouteExecutorFactory;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.json.JSValue;

public class JSONExecutor implements RouteExecutorFactory
{
    @Override
    public boolean match(final Method handler, final String[] as, final Router router)
    {
        return JSValue.class.isAssignableFrom(handler.getReturnType()) && handler.getParameterTypes().length == as.length && isParameters(handler.getParameterTypes());
    }

    protected boolean isParameters(Class<?>[] classes)
    {
        // if (classes.length < 1) return false;
        for (Class<?> clazz : classes)
        {
            if (!String.class.equals(clazz)) return false;
        }
        return true;
    }

    @Override
    public RouteExecutor compileExecutor(final Method handler, final String[] as, final Router router)
    {
        return new RouteExecutor()
        {
            public void execute(BalsaContext context) throws Throwable
            {
                try
                {
                    BalsaRequest request = context.getRequest();
                    BalsaResponse response = context.getResponse();
                    // bind variables
                    Object[] args = new Object[as.length];
                    for (int i = 0; i < args.length; i++)
                    {
                        Parameter p = request.getParameter(as[i]);
                        if (p != null) args[i] = p.getStringValue();
                    }
                    // process
                    JSValue res = (JSValue) handler.invoke(router, args);
                    // output
                    response.ok();
                    response.json();
                    response.getJsonWriter().writeValue(res);
                    response.flush();
                }
                catch (IllegalAccessException e)
                {
                    throw new BalsaInternalError("Error executing route handler method", e);
                }
                catch (IllegalArgumentException e)
                {
                    throw new BalsaInternalError("Error executing route handler method", e);
                }
                catch (InvocationTargetException e)
                {
                    throw e.getTargetException();
                }
            }
        };
    }
}
