package com.intrbiz.balsa.listener.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.http.HTTP.CacheControl;
import com.intrbiz.balsa.http.HTTP.Charsets;
import com.intrbiz.balsa.http.HTTP.ContentTypes;
import com.intrbiz.balsa.http.HTTP.Expires;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.util.CookieBuilder;
import com.intrbiz.balsa.util.HTMLWriter;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class BalsaHTTPResponse implements BalsaResponse
{
    // Thu, 01 Jan 1970 00:00:00 GMT
    public final SimpleDateFormat HEADER_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    private HTTPStatus status = HTTPStatus.OK;

    private Charset charset = Charsets.UTF8;

    private String contentType = ContentTypes.TEXT_HTML;

    private String cacheControl = CacheControl.NO_CACHE;

    private String expires = Expires.EXPIRED;

    private List<Entry<String,String>> headers = new LinkedList<Entry<String,String>>();

    private boolean sentHeaders = false;
    
    private ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();

    private Writer writer = null;

    private HTMLWriter htmlWriter = null;

    private final JsonFactory jsonFactory;
    
    private final YAMLFactory yamlFactory;

    private final XMLOutputFactory xmlFactory;

    private JsonGenerator jsonGenerator = null;
    
    private YAMLGenerator yamlGenerator = null;

    private XMLStreamWriter xmlWriter = null;
    
    public BalsaHTTPResponse(JsonFactory jsonFactory, XMLOutputFactory xmlFactory, YAMLFactory yamlFactory)
    {
        super();
        this.jsonFactory = jsonFactory;
        this.xmlFactory = xmlFactory;
        this.yamlFactory = yamlFactory;
    }
    
    @Override
    public void abortOnError(Throwable t) throws BalsaInternalError
    {
        if (this.isHeadersSent()) throw new IllegalStateException("An error occurred after data has been flushed to the web server, error handling cannot happen.", t);
        // Reset the response
        this.responseBuffer = new ByteArrayOutputStream();
        this.writer = null;
        this.htmlWriter = null;
        this.status = HTTPStatus.OK;
        this.charset = Charsets.UTF8;
        this.contentType = ContentTypes.TEXT_HTML;
        this.sentHeaders = false;
        this.cacheControl = CacheControl.NO_CACHE;
        this.expires = Expires.EXPIRED;
        this.headers.clear();
    }

    @Override
    public HTTPStatus getStatus()
    {
        return status;
    }

    @Override
    public BalsaResponse status(HTTPStatus status)
    {
        this.status = status;
        return this;
    }

    @Override
    public BalsaResponse ok()
    {
        this.status(HTTPStatus.OK);
        return this;
    }

    @Override
    public BalsaResponse notFound()
    {
        this.status(HTTPStatus.NotFound);
        return this;
    }

    @Override
    public BalsaResponse error()
    {
        this.status(HTTPStatus.InternalServerError);
        return this;
    }

    @Override
    public BalsaResponse redirect(boolean permanent)
    {
        if (permanent)
            this.status(HTTPStatus.MovedPermanently);
        else
            this.status(HTTPStatus.Found);
        return this;
    }

    @Override
    public Charset getCharset()
    {
        return charset;
    }

    @Override
    public BalsaResponse charset(Charset charset)
    {
        this.charset = charset;
        return this;
    }

    @Override
    public String getContentType()
    {
        return contentType;
    }

    @Override
    public BalsaResponse contentType(String contentType)
    {
        this.contentType = contentType;
        return this;
    }

    @Override
    public BalsaResponse plain()
    {
        this.contentType(ContentTypes.TEXT_PLAIN);
        return this;
    }

    @Override
    public BalsaResponse html()
    {
        this.contentType(ContentTypes.TEXT_HTML);
        return this;
    }

    @Override
    public BalsaResponse javascript()
    {
        this.contentType(ContentTypes.TEXT_JAVASCRIPT);
        return this;
    }

    @Override
    public BalsaResponse json()
    {
        this.contentType(ContentTypes.APPLICATION_JSON);
        return this;
    }
    
    @Override
    public BalsaResponse yaml()
    {
        this.contentType(ContentTypes.TEXT_YAML);
        return this;
    }
    
    @Override
    public BalsaResponse xml()
    {
        this.contentType(ContentTypes.APPLICATION_XML);
        return this;
    }

    @Override
    public BalsaResponse css()
    {
        this.contentType(ContentTypes.TEXT_CSS);
        return this;
    }

    @Override
    public String getCacheControl()
    {
        return this.cacheControl;
    }

    @Override
    public BalsaResponse cacheControl(String value)
    {
        this.cacheControl = value;
        return this;
    }

    @Override
    public String getExpires()
    {
        return this.expires;
    }

    @Override
    public BalsaResponse expires(String value)
    {
        this.expires = value;
        return this;
    }

    @Override
    public BalsaResponse expires(Date value)
    {
        this.expires = HEADER_DATE_FORMAT.format(value);
        return this;
    }

    @Override
    public BalsaResponse header(String name, String value)
    {
        this.headers.add(new SimpleEntry<String,String>(name, value));
        return this;
    }

    @Override
    public BalsaResponse header(String name, Date value)
    {
        this.header(name, HEADER_DATE_FORMAT.format(value));
        return this;
    }
    
    public CookieBuilder<BalsaResponse> setCookie()
    {
        return new CookieBuilder<BalsaResponse>()
        {
            @Override
            public BalsaResponse set()
            {
                header("Set-Cookie", this.build());
                return BalsaHTTPResponse.this;
            }
        };
    }
    
    public CookieBuilder<BalsaResponse> cookie()
    {
        return this.setCookie();
    }

    @Override
    public BalsaResponse redirect(String location, boolean permanent) throws IOException
    {
        this.redirect(permanent);
        this.header("Location", location);
        return this;
    }

    @Override
    public List<String> getHeaders()
    {
        return this.headers.stream().map((e) -> e.getKey() + ": " + e.getValue()).collect(Collectors.toList());
    }

    @Override
    public BalsaResponse sendHeaders() throws IOException
    {
        if (!this.sentHeaders)
        {
            this.sentHeaders = true;
            // currently we completely buffer the response
        }
        return this;
    }
    
    @Override
    public BalsaResponse sendFile(String file) throws IOException
    {
        throw new IOException("Not supported");
    }

    @Override
    public OutputStream getOutput() throws IOException
    {
        this.sendHeaders();
        return this.responseBuffer;
    }

    @Override
    public Writer getWriter() throws IOException
    {
        if (this.writer == null) this.writer = new BufferedWriter(new OutputStreamWriter(this.getOutput(), this.getCharset()), 8192);
        return writer;
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
    public YAMLGenerator getYamlWriter() throws IOException
    {
        if (this.yamlGenerator == null)
        {
            this.yamlGenerator = this.yamlFactory.createGenerator(this.getWriter());
            this.yamlGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
        }
        return this.yamlGenerator;
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
        if (this.htmlWriter == null) this.htmlWriter = new HTMLWriter(this.getWriter());
        return this.htmlWriter;
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
            if (this.jsonGenerator != null)
            {
                this.jsonGenerator.flush();
            }
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
            if (this.htmlWriter != null)
            {
                this.htmlWriter.flush();
            }
            else if (this.writer != null)
            {
                this.writer.flush();
            }
        }
        return this;
    }

    @Override
    public boolean isHeadersSent()
    {
        return this.sentHeaders;
    }
    
    void sendResponse(ChannelHandlerContext ctx)
    {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(this.status.getCode()), Unpooled.wrappedBuffer(this.responseBuffer.toByteArray()));
        // headers
        response.headers().set(CONTENT_TYPE, this.getContentType() /*+ "; charset=" + this.getCharset().toString().toLowerCase()*/);
        response.headers().set(EXPIRES, this.expires);
        response.headers().set(CACHE_CONTROL, this.cacheControl);
        response.headers().set(SERVER, "Balsa HTTP Listener");
        for (Entry<String, String> header : this.headers)
        {
            response.headers().set(header.getKey(), header.getValue());
        }
        // send
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
