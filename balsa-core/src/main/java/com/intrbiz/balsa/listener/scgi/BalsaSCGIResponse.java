package com.intrbiz.balsa.listener.scgi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.listener.BalsaResponse;
import com.intrbiz.balsa.scgi.SCGIResponse;
import com.intrbiz.balsa.scgi.SCGIResponse.Status;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.json.writer.JSONWriter;

public final class BalsaSCGIResponse implements BalsaResponse
{
    private final SCGIResponse res;
    
    public BalsaSCGIResponse(SCGIResponse res)
    {
        this.res = res;
    }

    @Override
    public void abortOnError(Throwable t) throws BalsaInternalError
    {
        this.res.abortOnError(t);
    }

    @Override
    public Status getStatus()
    {
        return this.res.getStatus();
    }

    @Override
    public void status(Status status)
    {
        this.res.status(status);
    }

    @Override
    public void ok()
    {
        this.res.ok();
    }

    @Override
    public void notFound()
    {
        this.res.notFound();
    }

    @Override
    public void error()
    {
        this.res.error();
    }

    @Override
    public void redirect(boolean permanent)
    {
        this.res.redirect(permanent);
    }

    @Override
    public Charset getCharset()
    {
        return this.res.getCharset();
    }

    @Override
    public void charset(Charset charset)
    {
        this.res.charset(charset);
    }

    @Override
    public String getContentType()
    {
        return this.res.getContentType();
    }

    @Override
    public void contentType(String contentType)
    {
        this.res.contentType(contentType);
    }

    @Override
    public void plain()
    {
        this.res.plain();
    }

    @Override
    public void html()
    {
        this.res.html();
    }

    @Override
    public void javascript()
    {
        this.res.javascript();
    }

    @Override
    public void json()
    {
        this.res.json();
    }

    @Override
    public void css()
    {
        this.res.css();
    }

    @Override
    public String getCacheControl()
    {
        return this.res.getCacheControl();
    }

    @Override
    public void cacheControl(String value)
    {
        this.res.cacheControl(value);
    }

    @Override
    public String getExpires()
    {
        return this.res.getExpires();
    }

    @Override
    public void expires(String value)
    {
        this.res.expires(value);
    }

    @Override
    public void expires(Date value)
    {
        this.res.expires(value);
    }

    @Override
    public void header(String name, String value)
    {
        this.res.header(name, value);
    }

    @Override
    public void header(String name, Date value)
    {
        this.res.header(name, value);
    }

    @Override
    public void redirect(String location, boolean permanent) throws IOException
    {
        this.res.redirect(location, permanent);
    }

    @Override
    public List<String> getHeaders()
    {
        return this.res.getHeaders();
    }

    @Override
    public void sendHeaders() throws IOException
    {
        this.res.sendHeaders();
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

    @Override
    public JSONWriter getJsonWriter() throws IOException
    {
        // TODO
        return null;
    }

    @Override
    public HTMLWriter getHtmlWriter() throws IOException
    {
        return this.res.getHtmlWriter();
    }

    @Override
    public void flush() throws IOException
    {
        this.res.flush();
    }

    @Override
    public boolean isHeadersSent()
    {
        return this.res.isHeadersSent();
    }
}
