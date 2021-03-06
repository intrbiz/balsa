package com.intrbiz.balsa.listener.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.intrbiz.Util;
import com.intrbiz.balsa.http.HTTP;
import com.intrbiz.balsa.http.HTTP.Charsets;
import com.intrbiz.balsa.http.HTTP.SCGI;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.parameter.ListParameter;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.parameter.StringParameter;
import com.intrbiz.balsa.scgi.middleware.QueryStringMiddleware;
import com.intrbiz.balsa.util.CookieSet;
import com.intrbiz.balsa.util.CookiesParser;
import com.intrbiz.balsa.util.ParameterSet;
import com.intrbiz.balsa.util.QueryStringParser;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

public class BalsaHTTPRequest implements BalsaRequest, ParameterSet, CookieSet
{
    private final ChannelHandlerContext ctx;
    
    private final FullHttpRequest req;
    
    private final JsonFactory jsonFactory;
    
    private final YAMLFactory yamlFactory;
    
    private final XMLInputFactory xmlFactory;
    
    private Map<String, String> headers = new HashMap<String, String>();
    
    private Map<String, Parameter> parameters = new HashMap<String, Parameter>();
    
    private Map<String, String> cookies = new HashMap<String, String>();
    
    private String uri;
    
    private String path;
    
    private String query;
    
    private String serverName = "localhost";
    
    private String serverAddress = "127.0.0.1";
    
    private int serverPort = 80;
    
    private long contentLength;
    
    private String contentType;
    
    private ByteBufInputStream input;
    
    private Reader reader;
    
    private JsonParser jsonReader;
    
    private YAMLParser yamlReader;
    
    private XMLStreamReader xmlReader;
    
    private Object body;
    
    public BalsaHTTPRequest(ChannelHandlerContext ctx, FullHttpRequest req, JsonFactory jsonFactory, XMLInputFactory xmlFactory, YAMLFactory yamlFactory)
    {
        super();
        this.ctx = ctx;
        this.req = req;
        this.jsonFactory = jsonFactory;
        this.xmlFactory = xmlFactory;
        this.yamlFactory = yamlFactory;
        // parse the request
        this.uri = this.req.getUri();
        // process the uri
        int idx = this.uri.indexOf("?");
        if (idx > 0)
        {
            this.path = this.cleanPath(this.uri.substring(0,  idx));
            this.query = this.uri.substring(idx + 1);
            // parse the QS
            QueryStringParser.parseQueryString(this.query, this);
        }
        else
        {
            this.path = this.cleanPath(this.uri);
            this.query = "";
        }
        // headers
        for (Entry<String, String> header : this.req.headers())
        {
           this.headers.put(header.getKey(), header.getValue());
           // pull data out that we need from the headers
           if (HttpHeaders.Names.COOKIE.equals(header.getKey()))
           {
               CookiesParser.parseCookies(header.getValue(), this);
           }
           else if (HttpHeaders.Names.CONTENT_TYPE.equals(header.getKey()))
           {
               String ct = header.getValue();
               int cts = ct.indexOf(';');
               if (cts > 0) ct = ct.substring(0, cts);
               this.contentType = ct.toLowerCase();
           }
           else if (HttpHeaders.Names.HOST.equals(header.getKey()))
           {
               String host = header.getValue();
               int hidx = host.indexOf(":");
               if (hidx > 0)
               {
                   this.serverName = host.substring(0, hidx);
                   this.serverPort = Integer.parseInt(host.substring(hidx + 1));
               }
               else
               {
                   this.serverName = host;
               }
           }
        }
        // content length
        this.contentLength = HttpHeaders.getContentLength(this.req, -1L);
        // parse the request body?
        // parse a posted query string
        if (this.contentType != null && this.contentType.startsWith(QueryStringMiddleware.WWW_FORM_URLENCODED))
        {
            // parse the parameters
            String postParameters = this.req.content().toString(Charsets.UTF8);
            QueryStringParser.parseQueryString(postParameters, this);
        }
    }
    
    private String cleanPath(String uri)
    {
        Stack<String> paths = new Stack<String>();
        // break up the path and decode each segment
        Matcher m = Pattern.compile("/").matcher(uri);
        int start = 0;
        while (m.find())
        {
            processPath(paths, uri.substring(start, m.start()));
            start = m.end();
        }
        processPath(paths, uri.substring(start));
        // join the path
        return Util.join("/", paths);
    }
    
    private void processPath(Stack<String> paths, String path)
    {
        try
        {
            String dPath = URLDecoder.decode(path, "UTF-8");
            if (".".equals(dPath))
            {
                // ignore
            }
            else if ("..".equals(dPath) && (! paths.empty()))
            {
                // go back up the path
                paths.pop();
            }
            else
            {
                paths.push(dPath);
            }
        }
        catch (UnsupportedEncodingException e)
        {
        }
    }

    @Override
    public void variable(String name, String value)
    {
    }

    @Override
    public int getContentLength()
    {
        return (int) this.contentLength;
    }

    @Override
    public String getContentType()
    {
        return this.contentType;
    }

    @Override
    public boolean isXml()
    {
        return HTTP.ContentTypes.APPLICATION_XML.equals(this.contentType);
    }

    @Override
    public boolean isJson()
    {
        return HTTP.ContentTypes.APPLICATION_JSON.equals(this.contentType);
    }
    
    @Override
    public boolean isYaml()
    {
        return HTTP.ContentTypes.TEXT_YAML.equals(this.contentType);
    }

    @Override
    public String getVersion()
    {
        return this.req.getProtocolVersion().text();
    }

    @Override
    public String getServerSoftware()
    {
        return "Balsa-HTTP-Listener";
    }

    @Override
    public String getServerName()
    {
        return this.serverName;
    }

    @Override
    public String getServerAddress()
    {
        return this.serverAddress;
    }

    @Override
    public int getServerPort()
    {
        return this.serverPort;
    }

    @Override
    public String getServerProtocol()
    {
        return this.req.getProtocolVersion().protocolName();
    }

    @Override
    public String getRemoteAddress()
    {
        return ((InetSocketAddress) this.ctx.channel().remoteAddress()).getAddress().getHostAddress();
    }

    @Override
    public int getRemotePort()
    {
        return ((InetSocketAddress) this.ctx.channel().remoteAddress()).getPort();
    }

    @Override
    public String getRequestMethod()
    {
        return this.req.getMethod().name();
    }

    @Override
    public String getRequestScheme()
    {
        return "http";
    }
    
    @Override
    public boolean isSecure()
    {
        return false;
    }

    @Override
    public String getRequestUri()
    {
        return this.path;
    }

    @Override
    public String getPathInfo()
    {
        return this.path;
    }

    @Override
    public String getQueryString()
    {
        return this.query;
    }

    @Override
    public String getScriptName()
    {
        return "";
    }

    @Override
    public String getScriptFileName()
    {
        return "";
    }

    @Override
    public String getDocumentRoot()
    {
        return "";
    }

    @Override
    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    @Override
    public String getHeader(String name)
    {
        return this.headers.get(name);
    }

    @Override
    public Set<String> getHeaderNames()
    {
        return this.headers.keySet();
    }

    @Override
    public Map<String, String> getVariables()
    {
        return new HashMap<String, String>();
    }

    @Override
    public String getVariable(String name)
    {
        return null;
    }

    @Override
    public Set<String> getVariableNames()
    {
        return new HashSet<String>();
    }

    @Override
    public Map<String, Parameter> getParameters()
    {
        return this.parameters;
    }

    @Override
    public Parameter getParameter(String name)
    {
        return this.parameters.get(name);
    }

    @Override
    public void addParameter(Parameter parameter)
    {
        this.parameters.put(parameter.getName(), parameter);
    }
    
    public void removeParameter(String name)
    {
        this.parameters.remove(name);
    }

    @Override
    public Set<String> getParameterNames()
    {
        return this.parameters.keySet();
    }

    @Override
    public Collection<Parameter> getParameterValues()
    {
        return this.parameters.values();
    }

    @Override
    public boolean containsParameter(String name)
    {
        return this.parameters.containsKey(name);
    }

    @Override
    public String cookie(String name)
    {
        return this.cookies.get(name);
    }

    @Override
    public void cookie(String name, String value)
    {
        this.cookies.put(name, value);
    }

    @Override
    public Map<String, String> cookies()
    {
        return this.cookies;
    }

    @Override
    public Set<String> cookieNames()
    {
        return this.cookies.keySet();
    }

    @Override
    public void removeCookie(String name)
    {
        this.cookies.remove(name);
    }

    @Override
    public InputStream getInput()
    {
        if (this.input == null)
        {
            this.input = new ByteBufInputStream(this.req.content());
        }
        return this.input;
    }

    @Override
    public Reader getReader()
    {
        if (this.reader == null)
        {
            this.reader = new InputStreamReader(this.getInput());
        }
        return this.reader;
    }

    public JsonParser getJsonReader() throws IOException
    {
        if (this.jsonReader == null)
        {
            this.jsonReader = this.jsonFactory.createParser(this.getReader());
        }
        return this.jsonReader;
    }
    
    public YAMLParser getYamlReader() throws IOException
    {
        if (this.yamlReader == null)
        {
            this.yamlReader = this.yamlFactory.createParser(this.getReader());
        }
        return this.yamlReader;
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
        return this.body;
    }

    @Override
    public void setBody(Object body)
    {
        this.body = body;
    }

    @Override
    public String dump()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getRequestMethod()).append(" ").append(this.getPathInfo()).append("\r\n\r\n");
        //
        sb.append(SCGI.CONTENT_LENGTH).append(": ").append(this.getContentLength()).append("\r\n");
        sb.append(SCGI.CONTENT_TYPE).append(": ").append(this.getContentType()).append("\r\n");
        sb.append(SCGI.SERVER_SOFTWARE).append(": ").append(this.getServerSoftware()).append("\r\n");
        sb.append(SCGI.SERVER_NAME).append(": ").append(this.getServerName()).append("\r\n");
        sb.append(SCGI.SERVER_ADDR).append(": ").append(this.getServerAddress()).append("\r\n");
        sb.append(SCGI.SERVER_PORT).append(": ").append(this.getServerPort()).append("\r\n");
        sb.append(SCGI.SERVER_PROTOCOL).append(": ").append(this.getServerProtocol()).append("\r\n");
        sb.append(SCGI.REMOTE_ADDR).append(": ").append(this.getRemoteAddress()).append("\r\n");
        sb.append(SCGI.REMOTE_PORT).append(": ").append(this.getRemotePort()).append("\r\n");
        sb.append(SCGI.REQUEST_METHOD).append(": ").append(this.getRequestMethod()).append("\r\n");
        sb.append(SCGI.REQUEST_SCHEME).append(": ").append(this.getRequestScheme()).append("\r\n");
        sb.append(SCGI.REQUEST_URI).append(": ").append(this.getRequestUri()).append("\r\n");
        sb.append(SCGI.PATH_INFO).append(": ").append(this.getPathInfo()).append("\r\n");
        sb.append(SCGI.QUERY_STRING).append(": ").append(this.getQueryString()).append("\r\n");
        //
        sb.append("\r\nHeaders:\r\n");
        for (Entry<String, String> hd : this.getHeaders().entrySet())
        {
            sb.append("\t").append(hd.getKey()).append(" => ").append(hd.getValue()).append("\r\n");
        }
        sb.append("\r\nCookies:\r\n");
        for (Entry<String, String> hd : this.cookies.entrySet())
        {
            sb.append("\t").append(hd.getKey()).append(" => ").append(hd.getValue()).append("\r\n");
        }
        sb.append("\r\nParameters:\r\n");
        for (Parameter p : this.getParameterValues())
        {
            if (p instanceof StringParameter)
            {
                sb.append("\t").append(p.getName()).append(" => ").append(p.getStringValue()).append("\r\n");
            }
            else if (p instanceof ListParameter)
            {
                for (Parameter v : p.getListValue())
                {
                    if (v instanceof StringParameter)
                    {
                        sb.append("\t").append(p.getName()).append(" => ").append(v.getStringValue()).append("\r\n");
                    }
                }
            }
        }
        return sb.toString();
    }
}
