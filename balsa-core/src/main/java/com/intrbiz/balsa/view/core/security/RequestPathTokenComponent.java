package com.intrbiz.balsa.view.core.security;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.value.ValueExpression;

public class RequestPathTokenComponent extends Component
{
    public RequestPathTokenComponent()
    {
        super();
    }
    
    public String getId()
    {
        ValueExpression ve = this.getAttribute("id");
        if (ve != null)
        {
            Object val = ve.get(BalsaContext.Balsa().getExpressContext(), this);
            if (val instanceof String) return (String) val;
        }
        return "request-token";
    }
    
    public String getPath()
    {
        ValueExpression ve = this.getAttribute("path");
        if (ve != null)
        {
            Object val = ve.get(BalsaContext.Balsa().getExpressContext(), this);
            if (val instanceof String) return (String) val;
        }
        return "/";
    }
}
