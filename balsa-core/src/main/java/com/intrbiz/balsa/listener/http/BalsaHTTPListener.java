package com.intrbiz.balsa.listener.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.listener.BalsaListener;

public class BalsaHTTPListener extends BalsaListener
{
    public static final int DEFAULT_PORT = 8080;

    private EventLoopGroup acceptGroup;

    private EventLoopGroup ioGroup;

    private ServerBootstrap server;

    private Channel serverChannel;
    
    private Logger logger = Logger.getLogger(BalsaHTTPListener.class);

    public BalsaHTTPListener()
    {
        super(DEFAULT_PORT);
    }

    public BalsaHTTPListener(int port, int poolSize)
    {
        super(port, poolSize);
    }

    public BalsaHTTPListener(int port)
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
    
    public String getListenerType()
    {
        return "http";
    }

    @Override
    public void start() throws BalsaException
    {
        try
        {
            logger.info("Listening for HTTP requests on port " + this.getPort());
            //
            this.acceptGroup = new NioEventLoopGroup(1);
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
                    ChannelPipeline p = ch.pipeline();
                    p.addLast("decoder", new HttpRequestDecoder());
                    p.addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
                    p.addLast("encoder", new HttpResponseEncoder());
                    p.addLast("handler", new BalsaHTTPHandler(BalsaHTTPListener.this.getBalsaApplication(), BalsaHTTPListener.this.getProcessor()));
                }
            });
            this.server.localAddress(new InetSocketAddress(8080));
            this.serverChannel = this.server.bind().sync().channel();
            //
            logger.trace("Listener started");
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
                this.acceptGroup.shutdownGracefully().sync();
                this.ioGroup.shutdownGracefully().sync();
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
