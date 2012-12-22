package com.intrbiz.balsa.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.scgi.SCGIResponse;
import com.intrbiz.balsa.scgi.SCGIResponse.Status;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.json.writer.JSONWriter;

/**
 * The current response
 */
public interface BalsaResponse
{
    /**
     * Common charsets
     */
    public static class Charsets extends SCGIResponse.Charsets
    {
    }

    /**
     * Common content types
     */
    public static class ContentTypes extends SCGIResponse.ContentTypes
    {
    }
    
    public static class CacheControl extends SCGIResponse.CacheControl
    {
    }
    
    public static class Expires extends SCGIResponse.Expires
    {
    }
    
    /**
     * Abort the response because an error has happened while processing the request.
     * 
     * Note: A reset can only happen if the response has not sent any data to the web server.
     * As such if reset is called and isHeadersSent() returns true a BalsaInternalError must be thrown
     *
     * @param t The error which caused the processing to be aborted.
     * @throws BalsaInternalError
     */
    public void abortOnError(Throwable t) throws BalsaInternalError;

    /**
     * Get the response HTTP status
     * 
     * @return returns int
     */
    public Status getStatus();

    /**
     * Set the status of the HTTP response
     * @param status 
     * returns void
     */
    public void status(Status status);

    /**
     * Set the HTTP response status to: 200
     */
    public void ok();

    /**
     * Set the HTTP response status to: 404
     */
    public void notFound();

    /**
     * Set the HTTP response status to: 500
     */
    public void error();

    /**
     * Set the HTTP response status to: 302
     */
    public void redirect(boolean permanent);

    /**
     * Get the character set of the response
     * 
     * @return returns Charset
     */
    public Charset getCharset();

    /**
     * Set the character set of the response
     * 
     * @param charset
     *            returns void
     */
    public void charset(Charset charset);

    /**
     * Get the response content type
     * 
     * @return returns String
     */
    public String getContentType();

    /**
     * Set the response content type
     * 
     * @param contentType
     *            The MIME content type
     */
    public void contentType(String contentType);

    /**
     * Set the response content type to: text/plain
     */
    public void plain();

    /**
     * Set the response content type to: text/html
     */
    public void html();

    /**
     * Set the response content type to: text/javascript
     */
    public void javascript();

    /**
     * Set the response content type to: application/json
     */
    public void json();

    /**
     * Set the response content type to: text/css
     */
    public void css();
    
    /**
     * Get the cache control header
     * @return
     * returns String
     */
    public String getCacheControl();
    
    /**
     * Set the cache control header
     * @param value
     * returns void
     */
    public void cacheControl(String value);
    
    /**
     * Get the expires header
     * @return
     * returns String
     */
    public String getExpires();
    
    /**
     * Set the expires header
     * @param value
     * returns void
     */
    public void expires(String value);
    public void expires(Date value);

    /**
     * Add a HTTP header to the response
     * 
     * @param name
     * @param value
     */
    public void header(String name, String value);
    
    public void header(String name, Date value);

    /**
     * Issue a redirect to the given location
     * 
     * @param location
     *            The redirection URL
     */
    public void redirect(String location, boolean permanent) throws IOException;

    /**
     * Get the headers which have been added to the response
     * 
     * @return returns List<String>
     */
    public List<String> getHeaders();

    /**
     * Flush the headers to the web server
     * 
     * @throws IOException
     */
    public void sendHeaders() throws IOException;

    /**
     * Get the raw response output stream
     * 
     * @return
     * @throws IOException
     *             returns OutputStream
     */
    public OutputStream getOutput() throws IOException;

    /**
     * Get the response writer (using the selected charset)
     * 
     * @return
     * @throws IOException
     *             returns Writer
     */
    public Writer getWriter() throws IOException;

    /**
     * Get the response JSON writer
     * 
     * @return
     * @throws IOException
     *             returns JSONWriter
     */
    public JSONWriter getJsonWriter() throws IOException;

    /**
     * Get the response HTML writer
     * 
     * @return
     * @throws IOException
     *             returns HTMLWriter
     */
    public HTMLWriter getHtmlWriter() throws IOException;
    
    /**
     * Flush this response to the web server
     * @throws IOException
     * returns void
     */
    public void flush() throws IOException;
    
    /**
     * Have the headers been sent to the web server
     */
    public boolean isHeadersSent();
}
