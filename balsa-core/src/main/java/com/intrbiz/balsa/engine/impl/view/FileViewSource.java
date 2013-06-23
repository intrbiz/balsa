package com.intrbiz.balsa.engine.impl.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaViewSource;

public class FileViewSource implements BalsaViewSource
{
    private Logger logger = Logger.getLogger(FileViewSource.class);
    
    public static final String DEFAULT_VIEW_PATH = "views";
    
    public static final String DEFAULT_EXTENSION = "xml";
    
    private final String extension;
    
    private final String format;
    
    private final Charset charset;
    
    public FileViewSource(String format, Charset charset, String extension)
    {
        super();
        this.format = format;
        this.extension = extension;
        this.charset = charset;
    }
    
    public FileViewSource()
    {
        this(BalsaViewSource.Formats.BALSA, BalsaViewSource.Charsets.UTF8, DEFAULT_EXTENSION);
    }
    
    @Override
    public Resource open(String name, BalsaContext context) throws BalsaException
    {
        List<File> path = context.app().getViewPath();
        for (File dir : path)
        {
            final File viewFile = new File(dir, name + "." + this.extension);
            logger.trace("Trying file: " + viewFile.getAbsolutePath());
            if (viewFile.exists() && (! viewFile.isDirectory()) && viewFile.canRead())
            {
                return new Resource(name, this.format, this.charset) {
                    @Override
                    public InputStream openStream() throws IOException
                    {
                        return new FileInputStream(viewFile);
                    }
                };
            }
        }
        return null;
    }
}
