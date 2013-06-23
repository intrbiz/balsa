package com.intrbiz.balsa.listener.scgi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.http.HTTP;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.scgi.SCGIResponse;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.util.HTMLWriter;

public final class BalsaSCGIResponse implements BalsaResponse
{
    // send file
    
    private static final String SEND_FILE_DEFAULT_HEADER = "X-SENDFILE";
    
    private static final String NGINX_ACCEL_HEADER = "X-Accel-Redirect";
    
    private static final String NGINX_SERVER_SOFTWARE = "nginx";
    
    //
    
    private final SCGIResponse res;
    
    private final BalsaRequest req;

    private final JsonFactory jsonFactory;

    private final XMLOutputFactory xmlFactory;

    private JsonGenerator jsonGenerator = null;

    private XMLStreamWriter xmlWriter = null;

    public BalsaSCGIResponse(SCGIResponse res, BalsaRequest req, JsonFactory jsonFactory, XMLOutputFactory xmlFactory)
    {
        this.res = res;
        this.req = req;
        this.jsonFactory = jsonFactory;
        this.xmlFactory = xmlFactory;
    }

    @Override
    public void abortOnError(Throwable t) throws BalsaInternalError
    {
        this.res.abortOnError(t);
    }

    @Override
    public HTTPStatus getStatus()
    {
        return this.res.getStatus();
    }

    @Override
    public BalsaResponse status(HTTPStatus status)
    {
        this.res.status(status);
        return this;
    }

    @Override
    public BalsaResponse ok()
    {
        this.res.ok();
        return this;
    }

    @Override
    public BalsaResponse notFound()
    {
        this.res.notFound();
        return this;
    }

    @Override
    public BalsaResponse error()
    {
        this.res.error();
        return this;
    }

    @Override
    public BalsaResponse redirect(boolean permanent)
    {
        this.res.redirect(permanent);
        return this;
    }

    @Override
    public Charset getCharset()
    {
        return this.res.getCharset();
    }

    @Override
    public BalsaResponse charset(Charset charset)
    {
        this.res.charset(charset);
        return this;
    }

    @Override
    public String getContentType()
    {
        return this.res.getContentType();
    }

    @Override
    public BalsaResponse contentType(String contentType)
    {
        this.res.contentType(contentType);
        return this;
    }

    @Override
    public BalsaResponse plain()
    {
        this.res.plain();
        return this;
    }

    @Override
    public BalsaResponse html()
    {
        this.res.html();
        return this;
    }

    @Override
    public BalsaResponse javascript()
    {
        this.res.javascript();
        return this;
    }

    @Override
    public BalsaResponse json()
    {
        this.res.json();
        return this;
    }
    
    @Override
    public BalsaResponse xml()
    {
        this.contentType(HTTP.ContentTypes.APPLICATION_XML);
        return this;
    }

    @Override
    public BalsaResponse css()
    {
        this.res.css();
        return this;
    }

    @Override
    public String getCacheControl()
    {
        return this.res.getCacheControl();
    }

    @Override
    public BalsaResponse cacheControl(String value)
    {
        this.res.cacheControl(value);
        return this;
    }

    @Override
    public String getExpires()
    {
        return this.res.getExpires();
    }

    @Override
    public BalsaResponse expires(String value)
    {
        this.res.expires(value);
        return this;
    }

    @Override
    public BalsaResponse expires(Date value)
    {
        this.res.expires(value);
        return this;
    }

    @Override
    public BalsaResponse header(String name, String value)
    {
        this.res.header(name, value);
        return this;
    }

    @Override
    public BalsaResponse header(String name, Date value)
    {
        this.res.header(name, value);
        return this;
    }

    @Override
    public BalsaResponse redirect(String location, boolean permanent) throws IOException
    {
        this.res.redirect(location, permanent);
        return this;
    }

    @Override
    public List<String> getHeaders()
    {
        return this.res.getHeaders();
    }

    @Override
    public BalsaResponse sendHeaders() throws IOException
    {
        this.res.sendHeaders();
        return this;
    }
    
    @Override
    public BalsaResponse sendFile(String file) throws IOException
    {
        // be smart based on the web server
        String serverSoftware = this.req.getServerSoftware();
        if (serverSoftware != null && (serverSoftware.toLowerCase().indexOf(NGINX_SERVER_SOFTWARE) != -1))
        {
            // nginx
            this.header(NGINX_ACCEL_HEADER, file);
        }
        else
        {
            // default to the defacto header
            this.header(SEND_FILE_DEFAULT_HEADER, file);
        }
        return this;
    }

    @Override
    public OutputStream getOutput() throws IOException
    {
        return this.res.getOutput();
    }

    @Override
    public Writer getWriter() throws IOException
    {
        return this.res.getWriter();
    }

    public BalsaResponse write(String content) throws IOException
    {
        Writer w = this.getWriter();
        w.write(content);
        return this;
    }

    @Override
    public JsonGenerator getJsonWriter() throws IOException
    {
        if (this.jsonGenerator == null)
        {
            this.jsonGenerator = this.jsonFactory.createGenerator(this.getWriter());
            this.jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
        }
        return this.jsonGenerator;
    }

    @Override
    public XMLStreamWriter getXMLWriter() throws IOException, XMLStreamException
    {
        if (this.xmlWriter == null)
        {
            this.xmlWriter = this.xmlFactory.createXMLStreamWriter(this.getWriter());
        }
        return this.xmlWriter;
    }

    @Override
    public HTMLWriter getHtmlWriter() throws IOException
    {
        return this.res.getHtmlWriter();
    }
    
    @Override
    public BalsaWriter getViewWriter() throws IOException
    {
        return this.getHtmlWriter();
    }

    @Override
    public BalsaResponse flush() throws IOException
    {
        // if the headers have not been sent, send them
        this.sendHeaders();
        // flush any streams we have opened first
        if (this.isHeadersSent())
        {
            if (this.jsonGenerator != null) this.jsonGenerator.flush();
            if (this.xmlWriter != null)
            {
                try
                {
                    this.xmlWriter.flush();
                }
                catch (XMLStreamException e)
                {
                    throw new IOException("Failed to flush XML Stream", e);
                }
            }
        }
        this.res.flush();
        return this;
    }

    @Override
    public boolean isHeadersSent()
    {
        return this.res.isHeadersSent();
    }
}
