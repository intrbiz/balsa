package com.intrbiz.balsa.view.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.renderer.Renderer;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.value.ValueExpression;

/**
 * A SAX handler which parses XML into Balsa component tree
 */
public class BalsaSAXHandler extends DefaultHandler
{
    private Stack<Component> stack = new Stack<Component>();
    
    private Stack<StringBuilder> textStack = new Stack<StringBuilder>();

    private ComponentLibraryRegister componentLibraryRegister;

    private RenderLibraryRegister renderLibraryRegister;

    private ParserContext context;

    private Logger logger = Logger.getLogger(BalsaSAXHandler.class);

    private BalsaContext balsaContext;

    public BalsaSAXHandler(BalsaView view, ComponentLibraryRegister clr, RenderLibraryRegister rlr, BalsaContext context)
    {
        this.context = new ParserContext(view);
        this.componentLibraryRegister = clr;
        this.renderLibraryRegister = rlr;
        this.balsaContext = context;
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        try
        {
            super.startPrefixMapping(prefix, uri);
            if ("".equals(prefix))
            {
                this.componentLibraryRegister.loadDefaultLibrary(uri.trim());
            }
            else
            {
                // load the component library
                this.componentLibraryRegister.loadLibrary(uri.trim());
            }
        }
        catch (BalsaException je)
        {
            throw new SAXException("Error loading component library: " + prefix + "=" + uri, je);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (length > 0)
        {
            Component parent = this.stack.peek();
            if (parent != null)
            {
                if (parent.coalesceText())
                {
                    StringBuilder sb = this.textStack.peek();
                    if (sb != null) sb.append(ch, start, length);
                }
                else
                {
                    String text = new String(ch, start, length).trim();
                    if (text.length() > 0)
                    {
                        parent.addText(this.parseText(text));
                    }
                }
            }
        }
    }
    
    private ValueExpression parseText(String text) throws SAXException
    {
        try
        {
            return new ValueExpression(this.balsaContext.getExpressContext(), text);
        }
        catch (ExpressException e)
        {
            throw new SAXException("Failed to parse text EL expression", e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (this.stack.isEmpty())
        {
            throw new SAXException("Could not close the element \"" + localName + "\".  It looks like the document is not well formed!");
        }
        else
        {
            Component comp = this.stack.pop();
            // coalesced text
            if (comp.coalesceText())
            {
                StringBuilder compText = this.textStack.pop();
                if (compText != null)
                {
                    String text = comp.preformattedText() ? compText.toString() : compText.toString().trim();
                    if (text.length() > 0)
                    {
                        comp.setText(this.parseText(text));
                    }
                }
            }
            // validate closing
            if (comp.getName() == null || !comp.getName().equalsIgnoreCase(localName))
            {
                throw new SAXException("Could not close the element \"" + localName + "\".  It looks like the document is not well formed!");
            }
            // merge
            if (this.stack.isEmpty())
            {
                this.stack.push(comp);
            }
            else
            {
                this.stack.peek().addChild(comp);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        try
        {
            Component comp = this.componentLibraryRegister.loadComponent(uri, localName);
            comp.setView(this.context.getView());
            // load the renderer
            comp.setRenderer((Renderer) this.renderLibraryRegister.loadRenderer(comp));
            for (int i = 0; i < attributes.getLength(); i++)
            {
                String name = attributes.getLocalName(i);
                String value = attributes.getValue(i);
                //
                ValueExpression valExp = new ValueExpression(this.balsaContext.getExpressContext(), value);
                comp.getAttributes().put(name, valExp);
            }
            this.stack.push(comp);
            if (comp.coalesceText()) this.textStack.push(new StringBuilder());
        }
        catch (Exception je)
        {
            throw new SAXException("Could not load component or renderer: " + uri + ", " + localName, je);
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        // define process instructions
        if ("RenderLibrary".equalsIgnoreCase(target))
        {
            try
            {
                this.renderLibraryRegister.loadLibrary(data);
            }
            catch (BalsaException je)
            {
                throw new SAXException("Error loading render library: " + data, je);
            }
        }
        else if ("PostProcessor".equalsIgnoreCase(target))
        {
            try
            {
                Class<?> clazz = Class.forName(data);
                PostProcessor pp = (PostProcessor) clazz.newInstance();
                this.context.getPostProcessors().add(pp);
            }
            catch (Exception e)
            {
                logger.error("Error loading post processor " + data, e);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException
    {
        Component root = this.stack.pop();
        this.context.setRoot(root);
        this.context.getPostProcessors().addAll(this.componentLibraryRegister.getRequiredPostProcessors());
    }

    @Override
    public void startDocument() throws SAXException
    {
    }

    public ParserContext getContext()
    {
        return this.context;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
    {
        if (BalsaDTD.Balsa_DTD_URI.equalsIgnoreCase(systemId))
            return new InputSource(new StringReader(BalsaDTD.Balsa_DTD));
        return super.resolveEntity(publicId, systemId);
    }
}
