package com.intrbiz.balsa.listener.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.InetSocketAddress;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.listener.BalsaListener;

public class HTTPListener extends BalsaListener
{
    public static final int DEFAULT_PORT = 8080;

    private EventLoopGroup acceptGroup;

    private EventLoopGroup ioGroup;

    private ServerBootstrap server;

    private Channel serverChannel;

    public HTTPListener()
    {
        super(DEFAULT_PORT);
    }

    public HTTPListener(int port, int poolSize)
    {
        super(port, poolSize);
    }

    public HTTPListener(int port)
    {
        super(port);
    }

    @Override
    public String getEngineName()
    {
        return "Balsa-HTTP-Listener";
    }
    
    public int getDefaultPort()
    {
        return DEFAULT_PORT;
    }

    @Override
    public void start() throws BalsaException
    {
        try
        {
            System.out.println("Listening on port " + this.getPort());
            //
            this.acceptGroup = new NioEventLoopGroup(this.getPoolSize());
            this.ioGroup = new NioEventLoopGroup(this.getPoolSize());
            //
            this.server = new ServerBootstrap();
            this.server.group(this.acceptGroup, this.ioGroup);
            this.server.channel(NioServerSocketChannel.class);
            this.server.childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    ch.pipeline().addLast(
                            new HttpRequestDecoder(), 
                            new HttpChunkAggregator(1048576), 
                            new HttpResponseEncoder(),
                            new ChunkedWriteHandler(),
                            new HTTPHandler(HTTPListener.this.getBalsaApplication()));
                }
            });
            this.server.localAddress(new InetSocketAddress(this.getPort()));
            this.serverChannel = this.server.bind().sync().channel();
            //
            System.out.println("Listener started");
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to start HTTP listener", e);
        }
    }

    @Override
    public void shutdown()
    {
        try
        {
            try
            {
                this.serverChannel.close().sync();
            }
            finally
            {
                this.server.shutdown();
            }
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public void stop()
    {
    }
}
