package com.intrbiz.balsa.express;

import static com.intrbiz.balsa.BalsaContext.Balsa;

import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class RequestTokenFunction extends Function
{
    public RequestTokenFunction()
    {
        super("access_token");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        return Balsa().generateAccessToken();
    }
}
