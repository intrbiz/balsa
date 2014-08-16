package com.intrbiz.balsa.view.component;

import static com.intrbiz.balsa.BalsaContext.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Add;
import com.intrbiz.express.operator.StringLiteral;
import com.intrbiz.express.value.ValueExpression;

/**
 * Component is the critical abstraction in balsa. A component effectively a controller which operates upon a small and specific part of a HTTP request.
 * 
 * Components are responsible for handling data between rendering and data. Where the data is held in beans and rendering is preformed by a Renderer.
 * 
 * Components are generally serialised as schemaless XML and therefore follow a DOM style pattern. A component has a name, text, parent Component, child List<Component> and a Map<String,ValueExpression> of attributes. Components therefore form a tree structure. Every component has an id, this is serialised in XML as an attribute however it is not stored within a components attribute map. Every component must have an ID, this is enforced by the deserialisation process.
 * 
 * In balsa a request has a series of phases, during this phases the component tree is processed. Therefore the component has certain lifecycle methods which are invoked in order to process these phases. Components are responsible to ensuring their children are correctly processed. In general components delegate these lifecycle method to there renderer, if they are to be rendered.
 */
public abstract class Component
{
    protected BalsaView view;

    /**
     * The component name, unique within a library
     */
    protected String name;

    /**
     * The parent component
     */
    protected Component parent;

    /**
     * The child components
     */
    protected List<Component> children = new LinkedList<Component>();

    /**
     * The component attributes, these will map to data, be it static or dynamic. Attribute values are pre-compiled EL expressions.
     */
    protected Map<String, ValueExpression> attributes = new TreeMap<String, ValueExpression>();

    /**
     * The textual value of this component
     */
    protected ValueExpression text;

    /**
     * The renderer to be used to render this component
     */
    protected Renderer renderer;
    
    /**
     * Does this component have any text nodes
     */
    private boolean textNodes = false;

    public Component()
    {
        super();
    }

    public BalsaView getView()
    {
        return view;
    }

    public void setView(BalsaView view)
    {
        this.view = view;
    }

    /**
     * @return the attributes
     */
    public final Map<String, ValueExpression> getAttributes()
    {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public final void setAttributes(Map<String, ValueExpression> attributes)
    {
        this.attributes = attributes;
    }

    /**
     * @return the children
     */
    public final List<Component> getChildren()
    {
        return children;
    }

    /**
     * @param children
     *            the children to set
     */
    public final void setChildren(List<Component> children)
    {
        this.children = children;
    }
    
    /**
     * Get the sibling of this component 
     * @param relativePosition the position relative to this component
     * @return the sibling or null
     */
    public final Component getSibling(int relativePosition)
    {
        if (this.parent == null || this.parent.children == null) return null;
        int index = this.parent.children.indexOf(this);
        if (index == -1) return null;
        index += relativePosition;
        if (index < 0 || index >= this.parent.children.size()) return null;
        return this.parent.children.get(index);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the parent
     */
    public final Component getParent()
    {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public final void setParent(Component parent)
    {
        this.parent = parent;
    }

    /**
     * @return the text
     */
    public ValueExpression getText()
    {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(ValueExpression text)
    {
        this.text = text;
    }
    
    public void setText(String text)
    {
        this.setText(new ValueExpression(new StringLiteral(text, false)));
    }
    
    public boolean canMergeText()
    {
        return this.children.isEmpty();
    }
    
    public void mergeText(String text)
    {
        if (this.text == null)
        {
            this.setText(text);
        }
        else
        {
            this.setText(new ValueExpression(new Add(this.getText().getOperator(), new StringLiteral(text, false))));
        }
    }
    
    public void mergeText(ValueExpression text)
    {
        if (this.text == null)
        {
            this.setText(text);
        }
        else
        {
            this.setText(new ValueExpression(new Add(this.getText().getOperator(), text.getOperator())));
        }
    }
    
    public void addText(ValueExpression text)
    {
        if (this.text == null && this.children.isEmpty())
        {
            this.setText(text);
        }
        else if (this.canMergeText())
        {
            this.mergeText(text);
        }
        else
        {
            this.addChild(new TextNode(text));
        }
    }
    
    public void addText(String text)
    {
        if (this.text == null && this.children.isEmpty())
        {
            this.setText(text);
        }
        else if (this.canMergeText())
        {
            this.mergeText(text);
        }
        else
        {
            this.addChild(new TextNode(text));
        }
    }

    /**
     * @return the renderer
     */
    public Renderer getRenderer()
    {
        return renderer;
    }

    /**
     * @param renderer
     *            the renderer to set
     */
    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    /**
     * 
     * @param context
     * @throws BalsaException
     *             returns void
     */
    public void decode(BalsaContext context) throws BalsaException, ExpressException
    {
        if (this.isRendered())
        {
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).decodeStart(this, context);
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).decodeChildren(this, context);
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).decodeEnd(this, context);
        }
    }

    /**
     * 
     * @param context
     * @throws BalsaException
     *             returns void
     */
    public void encode(BalsaContext context, BalsaWriter to) throws IOException, BalsaException, ExpressException
    {
        if (this.isRendered())
        {
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).encodeStart(this, context, to);
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).encodeChildren(this, context, to);
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).encodeEnd(this, context, to);
        }
    }

    /**
     * Add a child component
     * 
     * @param comp
     *            returns void
     */
    public final void addChild(Component comp)
    {
        comp.setParent(this);
        this.children.add(comp);
    }

    /**
     * Add an attributes
     * 
     * @param name
     * @param value
     *            returns void
     */
    public final void addAttribute(String name, ValueExpression value)
    {
        this.attributes.put(name, value);
    }

    /**
     * Get an attribute
     * 
     * @param name
     * @return returns ValueExpression
     */
    public final ValueExpression getAttribute(String name)
    {
        return this.attributes.get(name);
    }

    /**
     * Should this component and children be rendered
     * 
     * @return returns boolean
     */
    public boolean isRendered()
    {
        ValueExpression rend = this.getAttribute("rendered");
        if (rend != null)
        {
            try
            {
                Object obj = rend.get(Balsa().getExpressContext(), this);
                if (obj instanceof Boolean) return ((Boolean) obj).booleanValue();
            }
            catch (Exception e)
            {
            }
        }
        return true;
    }

    /**
     * An event invoked as soon as the view is loaded
     */
    public void load(BalsaContext context) throws BalsaException
    {
        if (this.getRenderer() != null) this.getRenderer().load(this, context);
        for (Component child : this.getChildren())
        {
            if (child instanceof TextNode) this.textNodes = true;
            child.load(context);
        }
    }
    
    /**
     * Does this component have any text nodes
     * Note: this is pre-computed during load()
     */
    public final boolean hasTextNodes()
    {
        return this.textNodes;
    }
    
    /**
     * Does this component have any text
     * @return
     */
    public final boolean hasText()
    {
        return this.text != null;
    }
    
    /**
     * Should the parse coalesce text for this node
     */
    public boolean coalesceText()
    {
        return false;
    }
    
    /**
     * Should the parser try to keep the text formatting
     */
    public boolean preformattedText()
    {
        return false;
    }
    
    /**
     * Is this component text span level
     */
    public boolean isSpan()
    {
       return false; 
    }

    public String toString()
    {
        return this.toString("");
    }

    public String toString(String p)
    {
        StringBuilder s = new StringBuilder();
        s.append(p).append("<").append(this.getName()).append(" renderer=\"").append(this.getRenderer() != null ? this.getRenderer().getClass().getSimpleName() : "").append("\"").append(">\n");
        for (Component child : this.getChildren())
        {
            s.append(child.toString(p + "    "));
        }
        s.append(p).append("</").append(this.getName()).append(">\r\n");
        return s.toString();
    }

    public String toXML(String p)
    {
        StringBuilder s = new StringBuilder();
        s.append(p).append("<").append(this.getName());
        //
        for (Entry<String, ValueExpression> attr : this.attributes.entrySet())
        {
            if (attr.getValue().getOperator() instanceof StringLiteral)
            {
                s.append(" ").append(attr.getKey()).append("=\"").append(Util.xmlEncode(((StringLiteral) attr.getValue().getOperator()).getValue())).append("\"");
            }
            else
            {
                s.append(" ").append(attr.getKey()).append("=\"").append(Util.xmlEncode(attr.getValue().toString())).append("\"");
            }
        }
        //
        s.append(">\r\n");
        //
        if (this.getText() != null)
        {
            if (this.getText().getOperator() instanceof StringLiteral)
            {
                String txt = ((StringLiteral) this.getText().getOperator()).getValue();
                if (! Util.isEmpty(txt))
                {
                    s.append(p).append("  ").append(txt).append("\r\n");
                }
            }
            else
            {
                String txt = this.getText().toString();
                if (!"#{''}".equals(txt))
                {
                    s.append(p).append("  ").append(txt).append("\r\n");
                }
            }
        }
        //
        for (Component child : this.getChildren())
        {
            s.append(child.toXML(p + "  "));
        }
        s.append(p).append("</").append(this.getName()).append(">\r\n");
        return s.toString();
    }
}
