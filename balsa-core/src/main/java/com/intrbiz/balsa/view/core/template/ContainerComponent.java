package com.intrbiz.balsa.view.core.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.value.ValueExpression;

public class ContainerComponent extends Component
{
    public ContainerComponent()
    {
        super();
    }
    
    /**
     * Get the map of variables which should be bound before encoding the this container
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
