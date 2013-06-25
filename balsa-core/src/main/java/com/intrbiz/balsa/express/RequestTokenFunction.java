package com.intrbiz.balsa.express;

import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

import static com.intrbiz.balsa.BalsaContext.Balsa;

public class RequestTokenFunction extends Function
{
    public RequestTokenFunction()
    {
        super("request_token");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        return Balsa().requestToken();
    }
}
