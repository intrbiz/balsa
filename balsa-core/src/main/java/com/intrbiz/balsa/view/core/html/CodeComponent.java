package com.intrbiz.balsa.view.core.html;

import com.intrbiz.balsa.view.component.Component;

public class CodeComponent extends Component
{
    public CodeComponent()
    {
        super();
    }
    
    public boolean coalesceText()
    {
        return true;
    }
    
    public boolean preformattedText()
    {
        return true;
    }
    
    public boolean isSpan()
    {
       return true; 
    }
}
