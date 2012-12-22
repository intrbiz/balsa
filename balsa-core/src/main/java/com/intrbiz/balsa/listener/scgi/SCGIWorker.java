package com.intrbiz.balsa.listener.scgi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaListener;
import com.intrbiz.balsa.listener.BalsaWorker;
import com.intrbiz.balsa.listener.BalsaRequest;

/**
 * A worker of the SCGI Listener.
 * 
 * The SCGI worker will parse the socket, populating a SCGIRequest.
 * 
 * The request is then passed to a SCGIProcessor for processing.
 * 
 */
public class SCGIWorker extends BalsaWorker
{
    private static final Charset SCGI_CHARSET = Charset.forName("ISO-8859-1");

    public SCGIWorker(BalsaListener listener, BlockingQueue<Socket> runQueue, ThreadFactory workerFactory)
    {
        super(listener, runQueue, workerFactory);
    }
    
    protected BalsaContext createbalsaContext(BalsaApplication application)
    {
        return new BalsaContext(application, new SCGIRequest(), new SCGIResponse());
    }

    protected void runClient(Socket client, BalsaContext context) throws Throwable
    {
        context.activate();
        try
        {
            InputStream input = new BufferedInputStream(client.getInputStream(), 1024);
            OutputStream output = new BufferedOutputStream(client.getOutputStream(), 8192);
            byte[] headers = this.readHeaders(input);
            this.parseHeaders(headers, context.getRequest());
            this.readBodySeparator(input);
            // set the streams
            context.getRequest().stream(input);
            context.getResponse().stream(output);
            // process
            this.getProcessor().process(context);
        }
        finally
        {
            // close
            context.deactivate();
            client.close();
        }

    }

    protected void parseHeaders(byte[] headers, BalsaRequest request)
    {
        int start = 0;
        String name = null;
        for (int i = 0; i < headers.length; i++)
        {
            if (headers[i] == 0)
            {
                if (name == null)
                {
                    name = new String(headers, start, i - start, SCGI_CHARSET);
                }
                else
                {
                    request.variable(name, new String(headers, start, i - start, SCGI_CHARSET));
                    name = null;
                }
                start = i + 1;
            }
        }
    }

    protected void readBodySeparator(InputStream input) throws IOException
    {
        int r = input.read();
        if (r == -1) throw new IOException("Unexpected EOF while reading body separator");
        if (r != ',') throw new IOException("SCGI body separator not found where it should be, got #" + r + " instead");
    }

    protected byte[] readHeaders(InputStream input) throws IOException
    {
        int headerLength = this.readHeaderLength(input);
        // Get a buffer for the headers
        byte[] buffer = new byte[headerLength];
        int readLength = 0;
        // read in the headers
        while (readLength < headerLength)
        {
            int r = input.read(buffer, readLength, headerLength);
            if (r == -1) throw new IOException("Unexpected EOF while reading headers");
            readLength += r;
        }
        return buffer;
    }

    protected int readHeaderLength(InputStream input) throws IOException
    {
        int r;
        int length = 0;
        while ((r = input.read()) != -1)
        {
            if (r == ':') return length;
            if (r < '0' && '9' > r) throw new IOException("Unexpected character #" + r + " in headers length");
            length = length * 10 + (r - '0');
        }
        throw new IOException("Unexpected EOF while reading header length");
    }
}
