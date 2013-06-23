package com.intrbiz.balsa.view.core.template;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;

public class IncludeComponent extends Component
{
    public IncludeComponent()
    {
        super();
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getViews(BalsaContext ctx) throws ExpressException
    {
        List<String> r = new LinkedList<String>();
        //
        ValueExpression vexp = this.getAttribute("view");
        if (vexp != null)
        {
            Object val = vexp.get(ctx.getExpressContext(), this);
            if (val instanceof Collection)
            {
                for (String s : (List<String>) val)
                {
                    r.add(s);
                }
            }
            else if (val instanceof String[])
            {
                for (String s : (String[]) val)
                {
                    r.add(s);
                }
            }
            else if (val instanceof String)
            {
                r.add((String) val);
            }
        }
        //
        return r;
    }
}
