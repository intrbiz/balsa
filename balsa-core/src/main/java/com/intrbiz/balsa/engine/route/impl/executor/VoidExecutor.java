package com.intrbiz.balsa.engine.route.impl.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.route.RouteExecutor;
import com.intrbiz.balsa.engine.route.RouteExecutorFactory;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaInternalError;

public class VoidExecutor implements RouteExecutorFactory
{
    @Override
    public boolean match(final Method handler, final String[] as, final Router router)
    {
        return void.class.equals(handler.getReturnType()) && handler.getParameterTypes().length == 0;
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
                    // process
                    handler.invoke(router, new Object[] {});
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
