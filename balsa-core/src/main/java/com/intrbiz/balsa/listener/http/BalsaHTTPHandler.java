package com.intrbiz.balsa.listener.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.xml.stream.XMLOutputFactory;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaProcessor;

public class BalsaHTTPHandler extends ChannelInboundHandlerAdapter
{
    private final BalsaApplication app;
    
    private final BalsaProcessor proc;
    
    private Logger logger = Logger.getLogger(BalsaHTTPHandler.class);
    
    private JsonFactory jsonFactory = new JsonFactory();
    
    private XMLOutputFactory xmlOutFactory = XMLOutputFactory.newFactory();
    
    // private XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();

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
            BalsaHTTPRequest  breq = new BalsaHTTPRequest(ctx, req);
            BalsaHTTPResponse bres = new BalsaHTTPResponse(this.jsonFactory, this.xmlOutFactory);
            BalsaContext bctx = new BalsaContext(this.app, breq, bres);
            BalsaContext.set(bctx);
            // process
            try
            {
                this.proc.process(bctx);
                bres.sendResponse(ctx);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
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
