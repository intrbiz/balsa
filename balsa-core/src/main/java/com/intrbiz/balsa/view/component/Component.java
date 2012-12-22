package com.intrbiz.balsa.view.component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.view.renderer.Renderer;
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
public abstract class Component implements Cloneable, BalsaView
{
    protected View view;

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
     * The component identifier, unique within one graph
     */
    protected String id = null;

    public Component()
    {
        super();
    }

    public View getView()
    {
        return view;
    }

    public void setView(View view)
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
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * 
     * @param context
     * @throws BalsaException
     *             returns void
     */
    public void decode(BalsaContext context) throws BalsaException
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
    public void encode(BalsaContext context) throws IOException, BalsaException
    {
        if (this.isRendered())
        {
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).encodeStart(this, context);
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).encodeChildren(this, context);
            if (this.getRenderer() != null) ((Renderer) this.getRenderer()).encodeEnd(this, context);
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

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Component) return this.id.equals(((Component) obj).getId());
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.id.hashCode();
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
                // TODO
                Object obj = rend.get(null, this);
                if (obj instanceof Boolean) return ((Boolean) obj).booleanValue();
            }
            catch (Exception e)
            {
                return true;
            }
        }
        return true;
    }

    /**
     * Deep copy
     * 
     * @return returns Component
     */
    public Component cloneComponent()
    {
        try
        {
            Component comp = (Component) this.clone();
            // copy attributes
            comp.attributes = new ConcurrentHashMap<String, ValueExpression>();
            for (String name : this.getAttributes().keySet())
            {
                ValueExpression value = this.getAttribute(name);
                comp.addAttribute(name, value);
            }
            // copy the children
            comp.children = new LinkedList<Component>();
            for (Component child : this.getChildren())
            {
                comp.addChild(child.cloneComponent());
            }
            if (this.getRenderer() != null) comp.setRenderer(((Renderer) this.getRenderer()).cloneRenderer());

            return comp;
        }
        catch (Exception e)
        {
        }
        return null;
    }

    /**
     * Deep clone of the component, prefixing the components id's with the given prefix
     * 
     * @param prefix
     * @return returns Component
     */
    public Component cloneComponent(String prefix)
    {
        try
        {
            Component comp = (Component) this.clone();
            comp.setId(prefix + " " + comp.getId());
            // copy attributes
            comp.attributes = new ConcurrentHashMap<String, ValueExpression>();
            for (String name : this.getAttributes().keySet())
            {
                ValueExpression value = this.getAttribute(name);
                comp.addAttribute(name, value);
            }
            // copy the children
            comp.children = new LinkedList<Component>();
            for (Component child : this.getChildren())
            {
                comp.addChild(child.cloneComponent(prefix));
            }
            if (this.getRenderer() != null) comp.setRenderer(((Renderer) this.getRenderer()).cloneRenderer());
            return comp;
        }
        catch (Exception e)
        {
        }
        return null;
    }

    /**
     * Shallow copy
     */
    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (Exception e)
        {
        }
        return null;
    }

    /**
     * Recursively search for a component with the given id
     * 
     * @param id
     * @return returns Component
     */
    public final Component getComponentById(String id)
    {
        return this.getComponentById(id, true);
    }

    /**
     * Search for a component either recursively or not
     * 
     * @param id
     * @param recurse
     *            - should child components be searched too
     * @return returns Component
     */
    public final Component getComponentById(String id, boolean recurse)
    {
        for (Component comp : this.children)
        {
            if (comp.getId().equals(id)) return comp;
            if (recurse)
            {
                Component found = comp.getComponentById(id);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * An event invoked as soon as the view is loaded
     * 
     * @param context
     * @throws BalsaException
     *             returns void
     */
    public void load(BalsaContext context) throws BalsaException
    {
        if (this.getRenderer() != null) this.getRenderer().load(this, context);
        for (Component child : this.getChildren())
        {
            child.load(context);
        }
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
