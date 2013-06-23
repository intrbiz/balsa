package com.intrbiz.balsa.view.core.data;

import java.io.IOException;
import java.util.Collection;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;

public class DataSetRenderer extends Renderer
{
    @Override
    public void encodeChildren(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        DataSetComponent dataSet = (DataSetComponent) component;
        //
        Object value = dataSet.evaluateValue();
        if (value != null)
        {
            if (!(value instanceof Collection)) throw new BalsaException("Cannot encode DataSet, the value must be a collection.");
            Collection<?> set = (Collection<?>) value;
            //
            String varName = dataSet.evaluateVar();
            String rowVarName = varName + "_rownum";
            //
            ExpressContext elctx = context.getExpressContext();
            //
            int row = 0;
            for (Object rowValue : set)
            {
                // we must enter a frame to isolate the variables
                elctx.enterFrame(false);
                try
                {
                    // set the variables
                    elctx.setEntity(rowVarName, row, dataSet);
                    elctx.setEntity(varName, rowValue, dataSet);
                    // encode all the children
                    for (Component child : dataSet.getChildren())
                    {
                        child.encode(context, to);
                    }
                }
                finally
                {
                    elctx.exitFrame();
                    row++;
                }
            }
        }
    }
}
