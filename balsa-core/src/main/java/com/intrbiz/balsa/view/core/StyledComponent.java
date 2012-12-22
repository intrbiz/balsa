package com.intrbiz.balsa.view.core;

import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.express.value.ValueExpression;

public class StyledComponent extends Component
{
    
    /*public String getStyleClass()
    {
        return this.getStringAttribute("class");
    }
    
    public void setStyleClass(String clazz)
    {
        this.addAttribute("class",clazz);
    }

    public String getStyle()
    {
        return this.getStringAttribute("style");
    }
    
    public void setStyle(String clazz)
    {
        this.addAttribute("style",clazz);
    }
    
    public String getTitle()
    {
        return this.getStringAttribute("title");
    }
    
    public void setTitle(String clazz)
    {
        this.addAttribute("title",clazz);
    }
    
    public String getAccessKey()
    {
        return this.getStringAttribute("accesskey");
    }
    
    public void setAccessKey(String x)
    {
        this.addAttribute("accesskey",x);
    }

    public String getTabIndex()
    {
        return this.getStringAttribute("tabindex");
    }
    
    public void setTabIndex(String x)
    {
        this.addAttribute("tabindex",x);
    }*/
    

    public ValueExpression getOnChange()
    {
        return this.getAttribute("onchange");
    }

    public void setOnChange(ValueExpression x)
    {
        this.addAttribute("onchange",x);
    }


    public ValueExpression getOnSubmit()
    {
        return this.getAttribute("onsubmit");
    }
    
    public void setOnSubmit(ValueExpression x)
    {
        this.addAttribute("onsubmit",x);
    }
    

    public ValueExpression getOnReset()
    {
        return this.getAttribute("onreset");
    }
    
    public void setOnReset(ValueExpression x)
    {
        this.addAttribute("onreset",x);
    }
    

    public ValueExpression getOnSelect()
    {
        return this.getAttribute("onselect");
    }
    
    public void setOnSelect(ValueExpression x)
    {
        this.addAttribute("onselect",x);
    }
    

    public ValueExpression getOnBlur()
    {
        return this.getAttribute("onblur");
    }
    
    public void setOnBlur(ValueExpression x)
    {
        this.addAttribute("onblur",x);
    }
    

    public ValueExpression getOnFocus()
    {
        return this.getAttribute("onfocus");
    }
    
    public void setOnFocus(ValueExpression x)
    {
        this.addAttribute("onfocus",x);
    }
    

    public ValueExpression getOnKeyDown()
    {
        return this.getAttribute("onkeydown");
    }
    
    public void setOnKeyDown(ValueExpression x)
    {
        this.addAttribute("onkeydown",x);
    }
    

    public ValueExpression getOnKeyUp()
    {
        return this.getAttribute("onkeyup");
    }
    
    public void setOnKeyUp(ValueExpression x)
    {
        this.addAttribute("onkeyup",x);
    }
    

    public ValueExpression getOnKeyPress()
    {
        return this.getAttribute("onkeypress");
    }
    
    public void setOnKeyPress(ValueExpression x)
    {
        this.addAttribute("onkeypress",x);
    }
    

    public ValueExpression getOnClick()
    {
        return this.getAttribute("onclick");
    }
    
    public void setOnClick(ValueExpression x)
    {
        this.addAttribute("onclick",x);
    }
    

    public ValueExpression getOnDoubleClick()
    {
        return this.getAttribute("ondblclick");
    }
    
    public void setOnDoubleClick(ValueExpression x)
    {
        this.addAttribute("ondblclick",x);
    }
    

    public ValueExpression getOnMouseDown()
    {
        return this.getAttribute("onmousedown");
    }
    
    public void setOnMousedown(ValueExpression x)
    {
        this.addAttribute("onmousedown",x);
    }
    

    public ValueExpression getOnMouseUp()
    {
        return this.getAttribute("onmouseup");
    }
    
    public void setOnMouseUp(ValueExpression x)
    {
        this.addAttribute("onmouseup",x);
    }
    

    public ValueExpression getOnMouseMove()
    {
        return this.getAttribute("onmousemove");
    }
    
    public void setOnMouseMove(ValueExpression x)
    {
        this.addAttribute("onmousemove",x);
    }
    

    public ValueExpression getOnMouseOver()
    {
        return this.getAttribute("onmouseover");
    }
    
    public void setOnMouseOver(ValueExpression x)
    {
        this.addAttribute("onmouseover",x);
    }
    

    public ValueExpression getOnMouseOut()
    {
        return this.getAttribute("onmouseout");
    }
    
    public void setOnMouseOut(ValueExpression x)
    {
        this.addAttribute("onmouseout",x);
    }
}
