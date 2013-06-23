package com.intrbiz.balsa.view.parser;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.view.component.Component;

public final class Parser
{
    public static Component parse(BalsaContext context, BalsaView view, Reader reader) throws BalsaException
    {
        return parse(context, view, new ComponentLibraryRegister(), new RenderLibraryRegister(), reader);
    }
    
    public static Component parse(BalsaContext context, BalsaView view, ComponentLibraryRegister clr, RenderLibraryRegister rlr, Reader reader) throws BalsaException
    {
        try
        {
            BalsaSAXHandler handler = new BalsaSAXHandler(view, clr, rlr, context);
            // use sax to parse the xml document
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.setEntityResolver(handler);
            xr.parse(new InputSource(reader));
            // get the context
            ParserContext pContext = handler.getContext();
            // run the post processors
            pContext.postProcess();
            Component root = pContext.getRoot();
            root.load(context);
            return root;
        }
        catch (SAXException e)
        {
            throw new BalsaException("Failed to parse view XML", e);
        }
        catch (IOException e)
        {
            throw new BalsaException("Failed to parse view", e);
        }
    }
}
