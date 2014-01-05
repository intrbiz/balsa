package com.intrbiz.balsa.listener.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.http.HTTP.ContentTypes;
import com.intrbiz.balsa.listener.BalsaFilter;
import com.intrbiz.balsa.util.Util;

public class PublicResourceFilter implements BalsaFilter
{
    public static final String DEV_PUBLIC_PATH = "src/main/public";
    
    private final Map<String, String> mimeTypeMappings = new TreeMap<String, String>();

    private final File dir;
    
    private Logger logger = Logger.getLogger(PublicResourceFilter.class);

    public PublicResourceFilter(File dir)
    {
        this.dir = dir;
        // default mime types;
        mimeTypeMappings.put("css", ContentTypes.TEXT_CSS);
        mimeTypeMappings.put("js", ContentTypes.TEXT_JAVASCRIPT);
        mimeTypeMappings.put("html", ContentTypes.TEXT_HTML);
        mimeTypeMappings.put("htm", ContentTypes.TEXT_HTML);
        mimeTypeMappings.put("xml", ContentTypes.APPLICATION_XML);
        mimeTypeMappings.put("txt", ContentTypes.TEXT_PLAIN);
        mimeTypeMappings.put("json", ContentTypes.APPLICATION_JSON);
        // images
        mimeTypeMappings.put("png", "image/png");
        mimeTypeMappings.put("tiff", "image/tiff");
        mimeTypeMappings.put("tif", "image/tiff");
        mimeTypeMappings.put("gif", "image/gif");
        mimeTypeMappings.put("jpeg", "image/jpeg");
        mimeTypeMappings.put("jpg", "image/jpeg");
        mimeTypeMappings.put("ico", "image/x-icon");
        mimeTypeMappings.put("svg", "image/svg+xml");
        mimeTypeMappings.put("svgz", "image/svg+xml");
        // audio
        mimeTypeMappings.put("mp3", "audio/mpeg");
        mimeTypeMappings.put("ogg", "audio/ogg");
    }

    @Override
    public void filter(BalsaContext context, BalsaFilterChain next) throws Throwable
    {
        File f = new File(this.dir, context.request().getPathInfo());
        logger.trace("Trying file: " + f.getAbsolutePath());
        if (f.exists() && f.isFile())
        {
            // return the file
            context.response().ok();
            // content type
            context.response().contentType(this.getMimeType(f));
            // content
            OutputStream out = context.response().getOutput();
            byte[] b = new byte[4096];
            int l;
            try (FileInputStream in = new FileInputStream(f))
            {
                while ((l = in.read(b)) != -1)
                {
                    out.write(b, 0, l);
                }
            }
        }
        else
        {
            next.filter(context);
        }
    }

    protected String getMimeType(File f)
    {
        // look at the file extention
        String name = f.getName();
        int idx = name.lastIndexOf(".");
        if (idx != -1)
        {
            String ext = name.substring(idx + 1);
            String mime = mimeTypeMappings.get(ext);
            if (!Util.isEmpty(mime)) return mime;
        }
        logger.trace("Could not find mime type for file '" + name + "', defaulting to text/plain");
        return ContentTypes.TEXT_PLAIN;
    }

    public Map<String, String> getMIME_TYPES()
    {
        return mimeTypeMappings;
    }

    public File getDir()
    {
        return dir;
    }
    
    public void addMimeTypeMapping(String extention, String mimeType)
    {
        this.mimeTypeMappings.put(extention, mimeType);
    }

    public String toString()
    {
        return "Public Resource Filter";
    }
}
