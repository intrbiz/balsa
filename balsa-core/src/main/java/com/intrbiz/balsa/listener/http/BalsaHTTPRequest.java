package com.intrbiz.balsa.listener.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonParser;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.parameter.Parameter;

public class BalsaHTTPRequest implements BalsaRequest
{
    private final ChannelHandlerContext ctx;
    
    private final HttpRequest req;
    
    private final String uri;
    
    public BalsaHTTPRequest(ChannelHandlerContext ctx, HttpRequest req, String uri)
    {
        super();
        this.ctx = ctx;
        this.req = req;
        this.uri = uri;
    }

    @Override
    public void variable(String name, String value)
    {
    }

    @Override
    public int getContentLength()
    {
        return 0;
    }

    @Override
    public String getContentType()
    {
        return null;
    }

    @Override
    public boolean isXml()
    {
        return false;
    }

    @Override
    public boolean isJson()
    {
        return false;
    }

    @Override
    public String getVersion()
    {
        return null;
    }

    @Override
    public String getServerSoftware()
    {
        return "Balsa-HTTP-Listener";
    }

    @Override
    public String getServerName()
    {
        return null;
    }

    @Override
    public String getServerAddress()
    {
        return null;
    }

    @Override
    public int getServerPort()
    {
        return 0;
    }

    @Override
    public String getServerProtocol()
    {
        return null;
    }

    @Override
    public String getRemoteAddress()
    {
        return null;
    }

    @Override
    public int getRemotePort()
    {
        return 0;
    }

    @Override
    public String getRequestMethod()
    {
        return null;
    }

    @Override
    public String getRequestScheme()
    {
        return null;
    }

    @Override
    public String getRequestUri()
    {
        return null;
    }

    @Override
    public String getPathInfo()
    {
        return null;
    }

    @Override
    public String getQueryString()
    {
        return null;
    }

    @Override
    public String getScriptName()
    {
        return null;
    }

    @Override
    public String getScriptFileName()
    {
        return null;
    }

    @Override
    public String getDocumentRoot()
    {
        return null;
    }

    @Override
    public Map<String, String> getHeaders()
    {
        return null;
    }

    @Override
    public String getHeader(String name)
    {
        return null;
    }

    @Override
    public Set<String> getHeaderNames()
    {
        return null;
    }

    @Override
    public Map<String, String> getVariables()
    {
        return null;
    }

    @Override
    public String getVariable(String name)
    {
        return null;
    }

    @Override
    public Set<String> getVariableNames()
    {
        return null;
    }

    @Override
    public Map<String, Parameter> getParameters()
    {
        return null;
    }

    @Override
    public Parameter getParameter(String name)
    {
        return null;
    }

    @Override
    public void addParameter(Parameter parameter)
    {
    }

    @Override
    public Set<String> getParameterNames()
    {
        return null;
    }

    @Override
    public Collection<Parameter> getParameterValues()
    {
        return null;
    }

    @Override
    public boolean containsParameter(String name)
    {
        return false;
    }

    @Override
    public String cookie(String name)
    {
        return null;
    }

    @Override
    public void cookie(String name, String value)
    {
    }

    @Override
    public InputStream getInput()
    {
        return null;
    }

    @Override
    public Reader getReader()
    {
        return null;
    }

    @Override
    public JsonParser getJsonReader() throws IOException
    {
        return null;
    }

    @Override
    public XMLStreamReader getXMLReader() throws IOException, XMLStreamException
    {
        return null;
    }

    @Override
    public Object getBody()
    {
        return null;
    }

    @Override
    public void setBody(Object body)
    {
    }

    @Override
    public String dump()
    {
        return null;
    }
}
