package com.intrbiz.balsa.listener.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import io.netty.channel.ChannelHandlerContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.util.BalsaWriter;
import com.intrbiz.balsa.util.HTMLWriter;

public class BalsaHTTPResponse implements BalsaResponse
{
    private final ChannelHandlerContext ctx;

    public BalsaHTTPResponse(ChannelHandlerContext ctx)
    {
        super();
        this.ctx = ctx;
    }

    @Override
    public void abortOnError(Throwable t) throws BalsaInternalError
    {
    }

    @Override
    public HTTPStatus getStatus()
    {
        return null;
    }

    @Override
    public BalsaResponse status(HTTPStatus status)
    {
        return null;
    }

    @Override
    public BalsaResponse ok()
    {
        return null;
    }

    @Override
    public BalsaResponse notFound()
    {
        return null;
    }

    @Override
    public BalsaResponse error()
    {
        return null;
    }

    @Override
    public BalsaResponse redirect(boolean permanent)
    {
        return null;
    }

    @Override
    public Charset getCharset()
    {
        return null;
    }

    @Override
    public BalsaResponse charset(Charset charset)
    {
        return null;
    }

    @Override
    public String getContentType()
    {
        return null;
    }

    @Override
    public BalsaResponse contentType(String contentType)
    {
        return null;
    }

    @Override
    public BalsaResponse plain()
    {
        return null;
    }

    @Override
    public BalsaResponse html()
    {
        return null;
    }

    @Override
    public BalsaResponse javascript()
    {
        return null;
    }

    @Override
    public BalsaResponse json()
    {
        return null;
    }

    @Override
    public BalsaResponse xml()
    {
        return null;
    }

    @Override
    public BalsaResponse css()
    {
        return null;
    }

    @Override
    public String getCacheControl()
    {
        return null;
    }

    @Override
    public BalsaResponse cacheControl(String value)
    {
        return null;
    }

    @Override
    public String getExpires()
    {
        return null;
    }

    @Override
    public BalsaResponse expires(String value)
    {
        return null;
    }

    @Override
    public BalsaResponse expires(Date value)
    {
        return null;
    }

    @Override
    public BalsaResponse header(String name, String value)
    {
        return null;
    }

    @Override
    public BalsaResponse header(String name, Date value)
    {
        return null;
    }

    @Override
    public BalsaResponse redirect(String location, boolean permanent) throws IOException
    {
        return null;
    }

    @Override
    public List<String> getHeaders()
    {
        return null;
    }

    @Override
    public BalsaResponse sendHeaders() throws IOException
    {
        return null;
    }

    @Override
    public BalsaResponse sendFile(String file) throws IOException
    {
        return null;
    }

    @Override
    public OutputStream getOutput() throws IOException
    {
        return null;
    }

    @Override
    public Writer getWriter() throws IOException
    {
        return null;
    }

    @Override
    public BalsaResponse write(String content) throws IOException
    {
        return null;
    }

    @Override
    public JsonGenerator getJsonWriter() throws IOException
    {
        return null;
    }

    @Override
    public HTMLWriter getHtmlWriter() throws IOException
    {
        return null;
    }

    @Override
    public BalsaWriter getViewWriter() throws IOException
    {
        return null;
    }

    @Override
    public XMLStreamWriter getXMLWriter() throws IOException, XMLStreamException
    {
        return null;
    }

    @Override
    public BalsaResponse flush() throws IOException
    {
        return null;
    }

    @Override
    public boolean isHeadersSent()
    {
        return false;
    }
}
