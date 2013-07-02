package com.intrbiz.balsa.express;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;
import com.intrbiz.express.operator.Operator;

public class RequestPathTokenFunction extends Function
{
    public RequestPathTokenFunction()
    {
        super("access_token_for_url");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        String path = "/";
        //
        Operator op = this.getParameter(0);
        if (op != null)
        {
            Object val = op.get(context, source);
            if (val instanceof String) path = (String) val; 
        }
        //
        return BalsaContext.Balsa().generateAccessTokenForURL(path);
    }

}
