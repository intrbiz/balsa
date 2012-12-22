package com.intrbiz.balsa.view.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;

public class FileLoader extends AbstractLoader
{
    public static final String DEFAULT_BASE = "views";
    
    private File base;
    
    private Logger logger = Logger.getLogger(FileLoader.class);

    public FileLoader(File base)
    {
        this.base = base;
    }

    @Override
    protected Reader read(String name) throws IOException, FileNotFoundException, BalsaException
    {
        return new InputStreamReader(new FileInputStream(new File(this.base, name + ".xml")), "UTF8");
    }

    public File getBase()
    {
        return base;
    }

    public void setBase(File base)
    {
        this.base = base;
        this.logger.info("View loader base path set to: " + base.getPath() + " (" + base.getAbsolutePath() + ")");
    }
}
