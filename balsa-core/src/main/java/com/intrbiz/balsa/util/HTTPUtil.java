package com.intrbiz.balsa.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static com.intrbiz.Util.isEmpty;

import com.intrbiz.balsa.listener.BalsaRequest;
import com.intrbiz.balsa.parameter.ListParameter;
import com.intrbiz.balsa.parameter.Parameter;
import com.intrbiz.balsa.parameter.StringParameter;

public class HTTPUtil
{
    public final static void parseQueryString(String query, BalsaRequest request)
    {
        if (!isEmpty(query))
        {
            int spos = 0, pos = 0;
            while ((pos = query.indexOf("&", spos)) != -1)
            {
                parseParameter(query.substring(spos, pos), request);
                spos = pos + 1;
            }
            parseParameter(query.substring(spos), request);
        }
    }

    public final static void parseParameter(String parameter, BalsaRequest request)
    {
        int pos = parameter.indexOf("=");
        if (pos != -1)
        {
            try
            {
                // TODO: Charset
                String name = URLDecoder.decode(parameter.substring(0, pos), "UTF-8");
                String value = URLDecoder.decode(parameter.substring(pos + 1), "UTF-8");
                Parameter jparam = new StringParameter(name, value);
                // add
                if (request.containsParameter(name))
                {
                    Parameter fparam = request.getParameter(name);
                    if (fparam instanceof ListParameter)
                    {
                        ((ListParameter) fparam).addValue(jparam);
                    }
                    else
                    {
                        request.addParameter(new ListParameter(name, fparam, jparam));
                    }
                }
                else
                {
                    request.addParameter(jparam);
                }
            }
            catch (UnsupportedEncodingException e)
            {
            }
        }
    }

    public final static void parseCookies(String cookies, BalsaRequest request)
    {
        if (!isEmpty(cookies))
        {
            int spos = 0, pos = 0;
            while ((pos = cookies.indexOf("; ", spos)) != -1)
            {
                parseCookie(cookies.substring(spos, pos), request);
                spos = pos + 1;
            }
            parseCookie(cookies.substring(spos), request);
        }
    }

    public final static void parseCookie(String cookie, BalsaRequest request)
    {
        int pos = cookie.indexOf("=");
        if (pos != -1) request.cookie(cookie.substring(0, pos), cookie.substring(pos + 1));
    }
}
