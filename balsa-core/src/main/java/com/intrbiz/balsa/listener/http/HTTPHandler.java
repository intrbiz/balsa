package com.intrbiz.balsa.listener.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.intrbiz.balsa.BalsaApplication;

public class HTTPHandler extends ChannelInboundMessageHandlerAdapter<HttpObject>
{
    private final BalsaApplication app;

    public HTTPHandler(BalsaApplication app)
    {
        super();
        this.app = app;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println("Context active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println("Context inactive");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception
    {
        if (msg instanceof HttpRequest)
        {
            HttpRequest req = (HttpRequest) msg;
            System.out.println("--------------------");
            System.out.println(req);
            System.out.println("--------------------");
            // bad request
            if (!req.getDecoderResult().isSuccess())
            {
                sendSimple(ctx, HttpResponseStatus.BAD_REQUEST, "text/plain; charset=UTF-8", "Bad Request");
                return;
            }
            // decode the URI
            String decodedUri = decodeURI(req.getUri());
            System.out.println("Decoded URI: " + decodedUri);
            // check public files
            File file = new File(new File("src/main/public"), decodedUri);
            System.out.println("File: " + file.getAbsolutePath());
            if (file.exists())
            {
                sendFile(ctx, file);
                return;
            }
            // invoke Balsa

            // default to not found
            sendSimple(ctx, HttpResponseStatus.NOT_FOUND, "text/plain; charset=UTF-8", "Not Found");
        }
        else
        {
            System.out.println("--------------------");
            System.out.println(msg);
            System.out.println("--------------------");
        }
    }

    private static String decodeURI(String uri)
    {
        System.out.println("Decoding URI: " + uri);
        try
        {
            uri = URLDecoder.decode(uri, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Error();
        }
        //
        return uri;
    }

    private static void sendSimple(final ChannelHandlerContext ctx, HttpResponseStatus status, String contentType, String content)
    {
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
        response.addHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
        response.addHeader(HttpHeaders.Names.SERVER, "Balsa HTTP Listener");
        response.setContent(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        //
        ctx.write(response);
        ctx.flush().addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendFile(final ChannelHandlerContext ctx, File file) throws IOException
    {
        try
        {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            //
            DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.addHeader(HttpHeaders.Names.CONTENT_TYPE, "text/css");
            response.addHeader(HttpHeaders.Names.SERVER, "Balsa HTTP Listener");
            response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, raf.length());
            //
            ctx.write(response);
            ctx.write(new ChunkedFile(raf, 8192));
            ctx.flush().addListener(ChannelFutureListener.CLOSE);
        }
        catch (FileNotFoundException e)
        {
            sendSimple(ctx, HttpResponseStatus.NOT_FOUND, "text/plain; charset=UTF-8", "Not Found");
        }
    }
}
