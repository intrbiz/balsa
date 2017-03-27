package com.intrbiz.balsa.view.core.html;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.value.ValueExpression;

public class FormComponent extends Component
{
    public FormComponent()
    {
        super();
    }
    
    public String getAccessToken()
    {
        ValueExpression ve = this.getAttribute("access-token");
        if (ve != null)
        {
            Object val = ve.get(BalsaContext.Balsa().getExpressContext(), this);
            return val == null ? null : val.toString();
        }
        return "access-token";
    }
}
