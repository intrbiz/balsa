package com.intrbiz.balsa.view.core.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    
    /**
     * Get the map of variables which should be bound before encoding the view
     */
    public Map<String, ValueExpression> getDataBindings()
    {
        Map<String, ValueExpression> bindings = new HashMap<String, ValueExpression>();
        for (Entry<String, ValueExpression> attribute : this.getAttributes().entrySet())
        {
            if (attribute.getKey().startsWith("data-"))
            {
                // strip of the 'data-' prefix
                bindings.put(attribute.getKey().substring(5), attribute.getValue());
            }
        }
        return bindings;
    }
}
