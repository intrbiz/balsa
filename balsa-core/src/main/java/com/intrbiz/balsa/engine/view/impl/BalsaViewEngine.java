package com.intrbiz.balsa.engine.view.impl;

import java.io.File;

import com.intrbiz.balsa.view.loader.FileLoader;

public class BalsaViewEngine extends AbstractBalsaViewEngine
{    
    public BalsaViewEngine()
    {
        super(new FileLoader(new File(FileLoader.DEFAULT_BASE)));
    }

    @Override
    public File getBase()
    {
        return ((FileLoader) this.viewLoader).getBase();
    }

    @Override
    public void base(File base)
    {
        ((FileLoader) this.viewLoader).setBase(base);
    }
}
