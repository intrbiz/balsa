package com.intrbiz.balsa.listener.middleware;

import java.io.IOException;
import java.io.InputStream;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.listener.BalsaMiddleware;

public class AbstractMiddleware implements BalsaMiddleware
{

    public boolean before(BalsaContext context) throws IOException
    {
        return true;
    }

    public void after(BalsaContext context) throws IOException
    {
    }

    protected byte[] readBodyBytes(BalsaRequest request) throws IOException
    {
        int length = request.getContentLength();
        byte[] buffer = new byte[length];
        InputStream input = request.getInput();
        int read = 0;
        while (read < length)
        {
            int r = input.read(buffer, read, length);
            if (r == -1) throw new IOException("Unexpected EOF while reading body");
            read += r;
        }
        return buffer;
    }
}
