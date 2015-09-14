package com.intrbiz.balsa.view.core.data;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;

public class DataSetRenderer extends Renderer
{
    private Collection<?> toCollection(Object value)
    {
        // no need to convert
        if (value instanceof Collection) return (Collection<?>) value;
        // convert to a list, otherwise empty
        List<Object> values = new LinkedList<Object>();
        if (value instanceof Iterable)
        {
            for (Object val : (Iterable<?>) values)
            {
                values.add(val);
            }
        }
        else if (value instanceof Object[])
        {
            for (Object val : (Object[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof double[])
        {
            for (double val : (double[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof float[])
        {
            for (float val : (float[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof long[])
        {
            for (long val : (long[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof int[])
        {
            for (int val : (int[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof short[])
        {
            for (short val : (short[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof byte[])
        {
            for (byte val : (byte[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof char[])
        {
            for (char val : (char[]) value)
            {
                values.add(val);
            }
        }
        else if (value instanceof boolean[])
        {
            for (boolean val : (boolean[]) value)
            {
                values.add(val);
            }
        }
        return values;
    }
    
    @Override
    public void encodeChildren(Component component, BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        DataSetComponent dataSet = (DataSetComponent) component;
        //
        Object value = dataSet.evaluateValue();
        if (value != null)
        {
            Collection<?> set = toCollection(value);
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
