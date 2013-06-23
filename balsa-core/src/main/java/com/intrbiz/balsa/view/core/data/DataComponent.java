package com.intrbiz.balsa.view.core.data;

import static com.intrbiz.balsa.BalsaContext.Balsa;

import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;

public class DataComponent extends Component
{
    private static final String VALUE = "value";
    private static final String VAR   = "var";
    
    public ValueExpression getValue()
    {
        return this.getAttribute(VALUE);
    }
    
    public void setValue(ValueExpression value)
    {
        this.addAttribute(VALUE, value);
    }
    
    public Object evaluateValue() throws ExpressException
    {
        return this.getValue().get(Balsa().getExpressContext(), this);
    }
    
    //
    
    public ValueExpression getVar()
    {
        return this.getAttribute(VAR);
    }
    
    public void setVar(ValueExpression var)
    {
        this.addAttribute(VAR, var);
    }
    
    public String evaluateVar() throws ExpressException
    {
        return (String) this.getVar().get(Balsa().getExpressContext(), this);
    }
}
