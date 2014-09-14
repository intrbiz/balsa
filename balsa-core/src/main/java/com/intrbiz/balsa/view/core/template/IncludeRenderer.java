package com.intrbiz.balsa.view.core.template;

import java.io.IOException;
import java.util.List;
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

public class IncludeRenderer extends Renderer
{
    @Override
    public void decodeChildren(Component component, BalsaContext context) throws BalsaException
    {
        
    }

    @Override
    public void encodeChildren(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        IncludeComponent inc = (IncludeComponent) component;
        List<String> views = inc.getViews(context);
        if (! views.isEmpty())
        {
            Map<String, ValueExpression> bindings = inc.getDataBindings();
            // enter an express frame to isolate variables
            ExpressContext elctx = context.getExpressContext();
            elctx.enterFrame(false);
            try
            {
                // set variables for each data binding
                for (Entry<String, ValueExpression> binding : bindings.entrySet())
                {
                    // Express is clever enough to support chaining of ValueExpressions, so we don't need to pre-evaluate them
                    elctx.setEntity(binding.getKey(), binding.getValue(), inc);
                }
                // encode the include
                context.encodeInclude(to, views.toArray(new String[views.size()]));
            }
            finally
            {
                elctx.exitFrame();
            }
        }
    }
}
