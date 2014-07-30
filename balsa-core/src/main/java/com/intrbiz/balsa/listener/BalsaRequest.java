package com.intrbiz.balsa.listener;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonParser;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.util.CookieSet;
import com.intrbiz.balsa.util.ParameterSet;

/**
 * The current request
 */
public interface BalsaRequest extends ParameterSet, CookieSet
{   
    /**
     * Set the value of a request variable
     * @param name The variable name
     * @param value The variable value
     */
    public void variable(String name, String value);
    
    /*
     * Request variables
     */

    /**
     * The content length of the request
     * @return
     * returns int
     */
    public int getContentLength();
    
    /**
     * The content type of the request
     * @return
     * returns String
     */
    public String getContentType();
    
    boolean isXml();
    
    boolean isJson();

    /**
     * The protocol version of the request
     * @return
     * returns String
     */
    public String getVersion();

    /**
     * The web server software, EG: Apache/2.2.22 (Mageia/PREFORK-2.mga1)
     * @return
     * returns String
     */
    public String getServerSoftware();

    /**
     * The web server name, EG: localhost
     * @return
     * returns String
     */
    public String getServerName();

    /**
     * The web server IP address, EG: 127.0.0.1
     * @return
     * returns String
     */
    public String getServerAddress();

    /**
     * The web server port, EG: 80
     * @return
     * returns int
     */
    public int getServerPort();

    /**
     * The web server protocol, EG: HTTP/1.1
     * @return
     * returns String
     */
    public String getServerProtocol();

    /**
     * The IP address of the client, EG: 127.0.0.1
     * @return
     * returns String
     */
    public String getRemoteAddress();

    /**
     * The port of the client, EG: 35709
     * @return
     * returns int
     */
    public int getRemotePort();

    /**
     * The HTTP request method, EG: GET
     * @return
     * returns String
     */
    public String getRequestMethod();
    
    /**
     * The HTTP request scheme, EG: http or https
     * @return
     * returns String
     */
    public String getRequestScheme();

    /**
     * The URI of the web request, EG: /test/
     * @return
     * returns String
     */
    public String getRequestUri();

    /**
     * The path info of the web request, EG: /
     * @return
     * returns String
     */
    public String getPathInfo();

    /**
     * The raw query string of the request, EG: a=test&b=test
     * @return
     * returns String
     */
    public String getQueryString();

    /**
     * The name of the script, as configured in the web server, EG: /test
     * @return
     * returns String
     */
    public String getScriptName();

    /**
     * The full URL of the script, EG: proxy:scgi://127.0.0.1:8090/
     * @return
     * returns String
     */
    public String getScriptFileName();

    /**
     * The document root of the web server, EG: /var/www/html
     * @return
     * returns String
     */
    public String getDocumentRoot();

    /**
     * Get the HTTP request headers
     * @return
     * returns Map<String,String>
     */
    public Map<String, String> getHeaders();

    /**
     * Get the value of the given HTTP header
     * @param name The HTTP header name
     * @return
     * returns String
     */
    public String getHeader(String name);

    /**
     * Get the names of all the HTTP headers
     * @return
     * returns Set<String>
     */
    public Set<String> getHeaderNames();

    /**
     * Get the HTTP request variables
     * @return
     * returns Map<String,String>
     */
    public Map<String, String> getVariables();
    
    /**
     * Get the value of the given HTTP request variable
     * @param name the variable name
     * @return
     * returns String
     */
    public String getVariable(String name);
    
    /**
     * Get the HTTP request variable names
     * @return
     * returns Set<String>
     */
    public Set<String> getVariableNames();
    
    /*
     * Parameters
     */

    /**
     * Get the request parameters
     * 
     * Request parameters are extracted from the query string and request body
     * 
     * @return
     * returns Map<String,Parameter>
     */
    public Map<String, Parameter> getParameters();
    
    /**
     * Get the value of the given request parameter
     * @param name The parameter name
     * @return
     * returns Parameter
     */
    public Parameter getParameter(String name);
    
    /**
     * Add a request parameter
     * @param parameter
     * returns void
     */
    public void addParameter(Parameter parameter);
    
    /**
     * Get the names of the request parameters
     * @return
     * returns Set<String>
     */
    public Set<String> getParameterNames();
    
    /**
     * Get the values of the request parameters
     * @return
     * returns Collection<Parameter>
     */
    public Collection<Parameter> getParameterValues();
    
    /**
     * Check that a parameter of the given name exists
     * @param name The parameter name
     * @return
     * returns boolean
     */
    public boolean containsParameter(String name);
    
    /**
     * Get the value of a cookie
     * @param name the cookie name
     * @return
     * returns String
     */
    public String cookie(String name);
    
    /**
     * Add a cookie to this request
     * @param name the cookie name
     * @param value the cookie value
     * returns void
     */
    public void cookie(String name, String value);
    
    /**
     * Get a map of all cookies
     * @return the Map of name to value of cookies
     */
    Map<String, String> cookies();
    
    /**
     * Get the set of all cookie names
     * @return a Set of cookie names
     */
    Set<String> cookieNames();
    
    /**
     * Remove a cookie
     * @param name the cookie name
     */
    void removeCookie(String name);
    
    /*
     * Body
     */
    
    /**
     * Get the request body as an input stream
     * @return
     * returns InputStream
     */
    public InputStream getInput();
    
    /**
     * Get the request body as a character reader
     * @return
     */
    Reader getReader();
    
    JsonParser getJsonReader() throws IOException;
    
    XMLStreamReader getXMLReader() throws IOException, XMLStreamException;

    /**
     * Get the parsed request body
     * @return
     * returns Object
     */
    public Object getBody();

    /**
     * Set the parsed request body
     * @param body
     * returns void
     */
    public void setBody(Object body);
    
    /*
     * Util
     */
    
    public String dump();
}
