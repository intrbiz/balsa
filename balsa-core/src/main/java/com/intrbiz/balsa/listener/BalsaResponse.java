package com.intrbiz.balsa.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.util.HTMLWriter;

/**
 * The current response
 */
public interface BalsaResponse
{    
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
    public HTTPStatus getStatus();

    /**
     * Set the status of the HTTP response
     * @param status 
     * returns void
     */
    public BalsaResponse status(HTTPStatus status);

    /**
     * Set the HTTP response status to: 200
     */
    public BalsaResponse ok();

    /**
     * Set the HTTP response status to: 404
     */
    public BalsaResponse notFound();

    /**
     * Set the HTTP response status to: 500
     */
    public BalsaResponse error();

    /**
     * Set the HTTP response status to: 302 or 301
     */
    public BalsaResponse redirect(boolean permanent);

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
    public BalsaResponse charset(Charset charset);

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
    public BalsaResponse contentType(String contentType);

    /**
     * Set the response content type to: text/plain
     */
    public BalsaResponse plain();

    /**
     * Set the response content type to: text/html
     */
    public BalsaResponse html();

    /**
     * Set the response content type to: text/javascript
     */
    public BalsaResponse javascript();

    /**
     * Set the response content type to: application/json
     */
    public BalsaResponse json();
    
    /**
     * Set the response content type to: application/xml
     */
    public BalsaResponse xml();

    /**
     * Set the response content type to: text/css
     */
    public BalsaResponse css();
    
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
    public BalsaResponse cacheControl(String value);
    
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
    public BalsaResponse expires(String value);
    public BalsaResponse expires(Date value);

    /**
     * Add a HTTP header to the response
     * 
     * @param name
     * @param value
     */
    public BalsaResponse header(String name, String value);
    
    public BalsaResponse header(String name, Date value);

    /**
     * Issue a redirect to the given location
     * 
     * @param location
     *            The redirection URL
     */
    public BalsaResponse redirect(String location, boolean permanent) throws IOException;

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
    public BalsaResponse sendHeaders() throws IOException;
    
    /**
     * Tell the web server to send the static file
     * @param file the file to send to the client
     * @throws IOException
     */
    public BalsaResponse sendFile(String file) throws IOException;

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
     * Helper to write a raw text response back
     * @param content
     * @return
     * @throws IOException
     */
    public BalsaResponse write(String content) throws IOException;

    /**
     * Get the response JSON writer
     * 
     * @return
     * @throws IOException
     *             returns JSONWriter
     */
    public JsonGenerator getJsonWriter() throws IOException;
    
    /**
     * Get the response HTML writer
     * 
     * @return returns HTMLWriter
     * @throws IOException
     */
    public HTMLWriter getHtmlWriter() throws IOException;

    /**
     * Get the response Balsa View writer
     * 
     * @return returns BalsaViewWriter
     * @throws IOException
     */
    public BalsaWriter getViewWriter() throws IOException;
    
    /**
     * Get the response XML writer
     * @return
     * @throws IOException
     */
    public XMLStreamWriter getXMLWriter() throws IOException, XMLStreamException;
    
    /**
     * Flush this response to the web server
     * @throws IOException
     * returns void
     */
    public BalsaResponse flush() throws IOException;
    
    /**
     * Have the headers been sent to the web server
     */
    public boolean isHeadersSent();
}
