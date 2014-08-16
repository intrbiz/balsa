package com.intrbiz.balsa.view.component;

import java.io.IOException;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.StringLiteral;
import com.intrbiz.express.value.ValueExpression;

public class TextNode extends Component
{
    protected boolean outputPadding = true;
    
    protected boolean outputNewline = true;
    
    public TextNode()
    {
        super();
    }
    
    public TextNode(String text)
    {
        this();
        this.setText(text);
    }
    
    public TextNode(ValueExpression text)
    {
        this();
        this.setText(text);
    }
    
    public String getName()
    {
        return "_text";
    }
    
    public String toXML(String p)
    {
        StringBuilder s = new StringBuilder();
        if (this.getText() != null)
        {
            if (this.getText().getOperator() instanceof StringLiteral)
            {
                String txt = ((StringLiteral) this.getText().getOperator()).getValue();
                if (! Util.isEmpty(txt))
                {
                    s.append(p).append("  ").append(Util.xmlEncode(txt)).append("\r\n");
                }
            }
            else
            {
                String txt = this.getText().toString();
                // TODO: looks bad
                if (!"#{''}".equals(txt))
                {
                    s.append(p).append("  ").append(Util.xmlEncode(txt)).append("\r\n");
                }
            }
        }
        return s.toString();
    }

    @Override
    public void decode(BalsaContext context) throws BalsaException, ExpressException
    {
    }

    @Override
    public void encode(BalsaContext context, BalsaWriter out) throws IOException, BalsaException, ExpressException
    {
        if (this.getText() != null)
        {
            try
            {
                if (this.outputPadding) out.padding();
                out.putEnc(String.valueOf(this.getText().get(context.getExpressContext(), this)));
                if (this.outputNewline) out.putLn("");
            }
            catch (ExpressException e)
            {
                throw new BalsaException("EL error", e);
            }
        }
    }

    @Override
    public void load(BalsaContext context) throws BalsaException
    {
        super.load(context);
        if (this.parent != null)
        {
            Component previousSibling = this.getSibling(-1);
            if (previousSibling instanceof TextNode || (previousSibling != null && previousSibling.isSpan()) || (previousSibling == null && this.parent.hasText()))
            {
                this.outputPadding = false;
            }
            Component nextSibling = this.getSibling(1);
            if (nextSibling instanceof TextNode || (nextSibling != null && nextSibling.isSpan()))
            {
                this.outputNewline = false;
            }
        }
    }
}
