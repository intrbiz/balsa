package com.intrbiz.balsa.view.component;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;

public class View extends BalsaView
{
    private Component root;

    public View(BalsaView previous)
    {
        super(previous);
    }

    public Component getRoot()
    {
        return root;
    }

    public void setRoot(Component root)
    {
        this.root = root;
    }

    @Override
    public void decode(BalsaContext context) throws BalsaException, ExpressException
    {
        this.root.decode(context);
    }

    @Override
    public void encode(BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        this.root.encode(context, to);
    }
    
    public String getTitle()
    {
        if (this.root == null) return null;
        ValueExpression titleExp = this.root.getAttribute("title");
        if (titleExp == null) return null;
        Object title = titleExp.get(BalsaContext.Balsa().getExpressContext(), this.root);
        if (title == null) return null;
        return String.valueOf(title);
    }
}
