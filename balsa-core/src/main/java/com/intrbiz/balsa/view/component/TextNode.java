package com.intrbiz.balsa.view.component;

import com.intrbiz.Util;
import com.intrbiz.express.operator.StringLiteral;
import com.intrbiz.express.value.ValueExpression;

public class TextNode extends Component
{
    
    public void setText(String text)
    {
        this.setText(new ValueExpression(new StringLiteral(text, false)));
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
}
