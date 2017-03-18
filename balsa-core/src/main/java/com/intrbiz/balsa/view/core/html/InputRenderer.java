package com.intrbiz.balsa.view.core.html;

import java.io.IOException;
import java.util.Map.Entry;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.parameter.StringParameter;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.converter.Converter;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;
import com.intrbiz.validator.Validator;

public class InputRenderer extends GenericRenderer
{
    protected void encodeAttributes(Component component, BalsaContext context, BalsaWriter out) throws IOException, BalsaException
    {
        for (Entry<String, ValueExpression> attribute : component.getAttributes().entrySet())
        {
            if ("checked".equalsIgnoreCase(attribute.getKey()))
            {
                this.encodeChecked(component, context, out, attribute.getKey(), attribute.getValue());
            }
            else
            {
                this.encodeAttribute(component, context, out, attribute.getKey(), attribute.getValue());
            }
        }
    }
    
    protected void encodeChecked(Component component, BalsaContext context, BalsaWriter out, String name, ValueExpression value) throws IOException, BalsaException
    {
        try
        {
            Object selectedValue = value.get(context.getExpressContext(), this);
            if (selectedValue instanceof Boolean)
            {
                if (((Boolean) selectedValue).booleanValue())
                    out.attribute(name, "checked");
            }
            else
            {
                out.attribute(name, String.valueOf(selectedValue));
            }
        }
        catch (ExpressException e)
        {
            throw new BalsaException("EL error", e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void decodeStart(Component component, BalsaContext context) throws BalsaException, ExpressException
    {
        ValueExpression value = component.getAttribute("value");
        if (value != null)
        {
            // eval the parameter name
            String inputName = component.getAttributeValue("name", context);
            if (inputName != null)
            {
                // do we have a parameter
                Parameter parameter = context.request().getParameter(inputName);
                if (parameter instanceof StringParameter)
                {
                    Object inputValue = parameter.getStringValue();
                    // convert?
                    Converter conv = value.getConverter(context.getExpressContext(), component);
                    if (conv != null) 
                        inputValue = conv.parseValue(parameter.getStringValue());
                    // validate?
                    Validator valid = value.getValidator(context.getExpressContext(), component);
                    if (valid != null)
                        valid.validate(inputValue);
                    // set
                    value.set(context.getExpressContext(), inputValue, component);
                }
            }
        }
    }
    
    
}
