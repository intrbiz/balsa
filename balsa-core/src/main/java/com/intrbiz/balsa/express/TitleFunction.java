package com.intrbiz.balsa.express;

import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class TitleFunction extends Function
{
    public TitleFunction()
    {
        super("title");
    }

    /**
     * Evaluate the title chain
     */
    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        if (source instanceof Component)
        {
            BalsaView view = ((Component) source).getView();
            String title = view.getNext().getTitle();
            return title == null ? "" : title;
        }
        return null;
    }
    
    @Override
    public boolean isIdempotent()
    {
        return false;
    }
}
