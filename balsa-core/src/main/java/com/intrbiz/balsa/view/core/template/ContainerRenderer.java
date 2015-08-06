package com.intrbiz.balsa.view.core.template;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;

public class ContainerRenderer extends Renderer
{
    @Override
    public void encodeChildren(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        ContainerComponent inc = (ContainerComponent) component;
        Map<String, ValueExpression> bindings = inc.getDataBindings();
        // enter an express frame to isolate variables
        ExpressContext elctx = context.getExpressContext();
        elctx.enterFrame(false);
        try
        {
            // set variables for each data binding
            for (Entry<String, ValueExpression> binding : bindings.entrySet())
            {
                // ensure that we pre-eval expressions, as the point is to locally cache a value
                elctx.setEntity(binding.getKey(), binding.getValue().get(elctx, component), inc);
            }
            // encode our children
            for (Component child : component.getChildren())
            {
                child.encode(context, to);
            }
        }
        finally
        {
            elctx.exitFrame();
        }
    }
}
