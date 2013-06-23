package com.intrbiz.balsa.express;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class PathInfoFunction extends Function
{
    public PathInfoFunction()
    {
        super("path_info");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        return BalsaContext.Balsa().request().getPathInfo();
    }

}
