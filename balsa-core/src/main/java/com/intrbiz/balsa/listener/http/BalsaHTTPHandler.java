package com.intrbiz.balsa.listener.http;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaProcessor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

public class BalsaHTTPHandler extends ChannelInboundHandlerAdapter
{
    private final BalsaApplication app;
    
    private final BalsaProcessor proc;
    
    private Logger logger = Logger.getLogger(BalsaHTTPHandler.class);
    
    private JsonFactory jsonFactory = new JsonFactory();
    
    private YAMLFactory yamlFactory = new YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID).enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
    
    private XMLOutputFactory xmlOutFactory = XMLOutputFactory.newFactory();
    
    private XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();

    public BalsaHTTPHandler(BalsaApplication app, BalsaProcessor proc)
    {
        this.app = app;
        this.proc = proc;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof FullHttpRequest)
        {
            FullHttpRequest req = (FullHttpRequest) msg;
            // parse the request
            logger.trace("HTTP Request: " + req.getMethod() + " " + req.getUri());
            BalsaHTTPRequest  breq = new BalsaHTTPRequest(ctx, req, this.jsonFactory, this.xmlInFactory, this.yamlFactory);
            BalsaHTTPResponse bres = new BalsaHTTPResponse(this.jsonFactory, this.xmlOutFactory, this.yamlFactory);
            BalsaContext bctx = new BalsaContext(this.app, breq, bres);
            BalsaContext.set(bctx);
            // process
            try
            {
                bctx.activate();
                try
                {
                    this.proc.process(bctx);
                    bres.sendResponse(ctx);
                }
                finally
                {
                    bctx.deactivate();
                }
            }
            catch (Error e)
            {
                throw e;
            }
            catch (Throwable t)
            {
                logger.debug("Error handling HTTP request", t);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
    {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        ctx.close();
    }
}
