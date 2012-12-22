package com.intrbiz.balsa.listener.scgi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.json.writer.JSONWriter;

public class SCGIResponse implements BalsaResponse
{
    // Thu, 01 Jan 1970 00:00:00 GMT
    public static final SimpleDateFormat HEADER_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    
    private OutputStream output;

    private Status status = Status.OK;

    private Charset charset = Charsets.UTF8;

    private String contentType = ContentTypes.TEXT_HTML;
    
    private String cacheControl = BalsaResponse.CacheControl.NO_CACHE;
    
    private String expires = BalsaResponse.Expires.EXPIRED;

    private List<String> headers = new LinkedList<String>();

    private boolean sentHeaders = false;

    private Writer writer = null;

    private JSONWriter jsonWriter = null;

    private HTMLWriter htmlWriter = null;

    public SCGIResponse()
    {
        super();
    }

    public void activate()
    {
    }

    public void deactivate()
    {
        this.output = null;
        this.writer = null;
        this.htmlWriter = null;
        this.jsonWriter = null;
        this.status = Status.OK;
        this.charset = Charsets.UTF8;
        this.contentType = ContentTypes.TEXT_HTML;
        this.sentHeaders = false;
        this.cacheControl = BalsaResponse.CacheControl.NO_CACHE;
        this.expires = BalsaResponse.Expires.EXPIRED;
        this.headers.clear();
    }
    
    public void abortOnError(Throwable t) throws BalsaInternalError
    {
        if (this.isHeadersSent()) throw new BalsaInternalError("An error occurred after data has been flushed to the web server, error handling cannot happen.", t);
        // Reset the response
        // Note: Do not reset the output stream!
        this.writer = null;
        this.htmlWriter = null;
        this.jsonWriter = null;
        this.status = Status.OK;
        this.charset = Charsets.UTF8;
        this.contentType = ContentTypes.TEXT_HTML;
        this.sentHeaders = false;
        this.cacheControl = BalsaResponse.CacheControl.NO_CACHE;
        this.expires = BalsaResponse.Expires.EXPIRED;
        this.headers.clear();
    }

    public void stream(OutputStream output)
    {
        this.output = output;
    }

    public Status getStatus()
    {
        return status;
    }

    public void status(Status status)
    {
        this.status = status;
    }

    public void ok()
    {
        this.status(Status.OK);
    }

    public void notFound()
    {
        this.status(Status.NotFound);
    }

    public void error()
    {
        this.status(Status.InternalServerError);
    }

    public void redirect(boolean permanent)
    {
        if (permanent) this.status(Status.MovedPermanently);
        else this.status(Status.Found);
    }

    public Charset getCharset()
    {
        return charset;
    }

    public void charset(Charset charset)
    {
        this.charset = charset;
    }

    public final String getContentType()
    {
        return contentType;
    }

    public void contentType(String contentType)
    {
        this.contentType = contentType;
    }

    public void plain()
    {
        this.contentType(ContentTypes.TEXT_PLAIN);
    }

    public void html()
    {
        this.contentType(ContentTypes.TEXT_HTML);
    }

    public void javascript()
    {
        this.contentType(ContentTypes.TEXT_JAVASCRIPT);
    }

    public void json()
    {
        this.contentType(ContentTypes.APPLICATION_JSON);
    }

    public void css()
    {
        this.contentType(ContentTypes.TEXT_CSS);
    }

    @Override
    public String getCacheControl()
    {
        return this.cacheControl;
    }

    @Override
    public void cacheControl(String value)
    {
        this.cacheControl = value;
    }

    @Override
    public String getExpires()
    {
        return this.expires;
    }

    @Override
    public void expires(Date value)
    {
        this.expires =  HEADER_DATE_FORMAT.format(value);
    }
    
    @Override
    public void expires(String value)
    {
        this.expires =  value;
    }

    @Override
    public void header(String name, Date value)
    {
        this.header(name, HEADER_DATE_FORMAT.format(value));
    }

    public void header(String name, String value)
    {
        this.headers.add(name + ": " + value);
    }

    public void redirect(String location, boolean permanent) throws IOException
    {
        this.redirect(permanent);
        this.header("Location", location);
        // flush the headers
        this.sendHeaders();
    }

    public List<String> getHeaders()
    {
        return headers;
    }

    public void sendHeaders() throws IOException
    {
        if (!this.sentHeaders)
        {
            this.sentHeaders = true;
            // create the writer
            Writer headerWriter = new BufferedWriter(new OutputStreamWriter(this.output, Charsets.SCGI), 1024);
            // the status
            headerWriter.write("Status: ");
            headerWriter.write(String.valueOf(this.getStatus().getCode()));
            headerWriter.write(" ");
            headerWriter.write(this.getStatus().getMessage());
            // the content type
            headerWriter.write("\r\nContent-Type: ");
            headerWriter.write(this.getContentType());
            headerWriter.write("; charset=");
            headerWriter.write(this.getCharset().name().toLowerCase());
            // cache control
            if (this.getCacheControl() != null)
            {
                headerWriter.write("\r\nCacheControl: ");
                headerWriter.write(this.getCacheControl());                
            }
            // expires
            if (this.getExpires() != null)
            {
                headerWriter.write("\r\nExpires: ");
                headerWriter.write(this.getExpires());                
            }
            // write headers
            for (String header : this.getHeaders())
            {
                headerWriter.write("\r\n");
                headerWriter.write(header);
            }
            // write end of headers
            headerWriter.write("\r\n\r\n");
            // flush
            headerWriter.flush();
        }
    }

    public OutputStream getOutput() throws IOException
    {
        this.sendHeaders();
        return output;
    }

    public Writer getWriter() throws IOException
    {
        if (this.writer == null) this.writer = new BufferedWriter(new OutputStreamWriter(this.getOutput(), this.getCharset()), 8192);
        return writer;
    }

    public JSONWriter getJsonWriter() throws IOException
    {
        if (this.jsonWriter == null) this.jsonWriter = new JSONWriter(this.getWriter());
        return this.jsonWriter;
    }

    public HTMLWriter getHtmlWriter() throws IOException
    {
        if (this.htmlWriter == null) this.htmlWriter = new HTMLWriter(this.getWriter());
        return this.htmlWriter;
    }

    public void flush() throws IOException
    {
        if (this.sentHeaders)
        {
            if (this.jsonWriter != null)
                this.jsonWriter.flush();
            else if (this.htmlWriter != null)
                this.htmlWriter.flush();
            else if (this.writer != null)
                this.writer.flush();
            else
                this.output.flush();
        }
    }
    
    public boolean isHeadersSent()
    {
        return this.sentHeaders;
    }
}
