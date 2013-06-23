package com.intrbiz.balsa.listener.scgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.intrbiz.balsa.http.HTTP;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.scgi.SCGIRequest;

public final class BalsaSCGIRequest implements BalsaRequest
{
    private final SCGIRequest req;
    
    private final JsonFactory jsonFactory;
    
    private final XMLInputFactory xmlFactory;
    
    private Reader reader;
    
    private JsonParser jsonReader;
    
    private XMLStreamReader xmlReader;
    
    public BalsaSCGIRequest(SCGIRequest req, JsonFactory jsonFactory, XMLInputFactory xmlFactory)
    {
        this.req = req;
        this.jsonFactory = jsonFactory;
        this.xmlFactory = xmlFactory;
    }

    @Override
    public void variable(String name, String value)
    {
        this.req.variable(name, value);
    }

    @Override
    public int getContentLength()
    {
        return this.req.getContentLength();
    }

    @Override
    public String getContentType()
    {
        return this.req.getContentType();
    }
    
    @Override
    public boolean isXml()
    {
        return HTTP.ContentTypes.APPLICATION_XML.equalsIgnoreCase(this.getContentType());
    }
    
    @Override
    public boolean isJson()
    {
        return HTTP.ContentTypes.APPLICATION_JSON.equalsIgnoreCase(this.getContentType());
    }

    @Override
    public String getVersion()
    {
        return this.req.getVersion();
    }

    @Override
    public String getServerSoftware()
    {
        return this.req.getServerSoftware();
    }

    @Override
    public String getServerName()
    {
        return this.req.getServerName();
    }

    @Override
    public String getServerAddress()
    {
        return this.req.getServerAddress();
    }

    @Override
    public int getServerPort()
    {
        return this.req.getServerPort();
    }

    @Override
    public String getServerProtocol()
    {
        return this.req.getServerProtocol();
    }

    @Override
    public String getRemoteAddress()
    {
        return this.req.getRemoteAddress();
    }

    @Override
    public int getRemotePort()
    {
        return this.req.getRemotePort();
    }

    @Override
    public String getRequestMethod()
    {
        return this.req.getRequestMethod();
    }
    
    @Override
    public String getRequestScheme()
    {
        return this.req.getRequestScheme();
    }

    @Override
    public String getRequestUri()
    {
        return this.req.getRequestUri();
    }

    @Override
    public String getPathInfo()
    {
        return this.req.getPathInfo();
    }

    @Override
    public String getQueryString()
    {
        return this.req.getQueryString();
    }

    @Override
    public String getScriptName()
    {
        return this.req.getScriptName();
    }

    @Override
    public String getScriptFileName()
    {
        return this.req.getScriptFileName();
    }

    @Override
    public String getDocumentRoot()
    {
        return this.req.getDocumentRoot();
    }

    @Override
    public Map<String, String> getHeaders()
    {
        return this.req.getHeaders();
    }

    @Override
    public String getHeader(String name)
    {
        return this.req.getHeader(name);
    }

    @Override
    public Set<String> getHeaderNames()
    {
        return this.req.getHeaderNames();
    }

    @Override
    public Map<String, String> getVariables()
    {
        return this.req.getVariables();
    }

    @Override
    public String getVariable(String name)
    {
        return this.req.getVariable(name);
    }

    @Override
    public Set<String> getVariableNames()
    {
        return this.req.getVariableNames();
    }

    @Override
    public Map<String, Parameter> getParameters()
    {
        return this.req.getParameters();
    }

    @Override
    public Parameter getParameter(String name)
    {
        return this.req.getParameter(name);
    }

    @Override
    public void addParameter(Parameter parameter)
    {
        this.req.addParameter(parameter);
    }

    @Override
    public Set<String> getParameterNames()
    {
        return this.req.getParameterNames();
    }

    @Override
    public Collection<Parameter> getParameterValues()
    {
        return this.req.getParameterValues();
    }

    @Override
    public boolean containsParameter(String name)
    {
        return this.req.containsParameter(name);
    }

    @Override
    public String cookie(String name)
    {
        return this.req.cookie(name);
    }

    @Override
    public void cookie(String name, String value)
    {
        this.req.cookie(name, value);
    }

    @Override
    public InputStream getInput()
    {
        return this.req.getInput();
    }
    
    public Reader getReader()
    {
        // TODO
        if (this.reader == null)
        {
            this.reader = new BufferedReader(new InputStreamReader(this.getInput(), HTTP.Charsets.UTF8));
        }
        return this.reader;
    }
    
    public JsonParser getJsonReader() throws IOException
    {
        if (this.jsonReader == null)
        {
            this.jsonReader = this.jsonFactory.createJsonParser(this.getReader());
        }
        return this.jsonReader;
    }
    
    public XMLStreamReader getXMLReader() throws IOException, XMLStreamException
    {
        if (this.xmlReader == null)
        {
            this.xmlReader = this.xmlFactory.createXMLStreamReader(this.getReader());
        }
        return this.xmlReader;
    }

    @Override
    public Object getBody()
    {
        return this.req.getBody();
    }

    @Override
    public void setBody(Object body)
    {
        this.req.setBody(body);
    }

    @Override
    public String dump()
    {
        return this.req.dumpRequest();
    }
}
