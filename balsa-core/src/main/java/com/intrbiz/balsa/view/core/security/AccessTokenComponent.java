package com.intrbiz.balsa.view.core.security;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.value.ValueExpression;

public class AccessTokenComponent extends Component
{
    public AccessTokenComponent()
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
        return "key";
    }
}
