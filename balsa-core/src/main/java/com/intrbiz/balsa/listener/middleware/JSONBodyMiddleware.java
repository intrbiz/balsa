package com.intrbiz.balsa.listener.middleware;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.json.JSONException;
import com.intrbiz.json.JSValue;
import com.intrbiz.json.reader.JSONReader;

public class JSONBodyMiddleware extends AbstractMiddleware
{
    private static final String JSON_MIME_TYPE = "application/json";
    
    @Override
    public boolean before(BalsaContext context) throws IOException
    {
        BalsaRequest request = context.getRequest();
        // parse the JSON request body
        if (JSON_MIME_TYPE.equals(request.getContentType()))
        {
            byte[] body = this.readBodyBytes(request);
            JSONReader jr = new JSONReader(new InputStreamReader(new ByteArrayInputStream(body)));
            try
            {
                JSValue jsonIn = jr.readValue();
                request.setBody(jsonIn);
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
        // continue processing the request
        return true;
    }
}
