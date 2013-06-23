package com.intrbiz.balsa;

import com.intrbiz.balsa.engine.impl.view.FileViewSource;
import com.intrbiz.balsa.engine.view.BalsaViewSource;

/**
 * Syntactic Sugar
 */
public class BalsaAPT
{
    /**
     * Enable APT support for the given application
     * @param application
     */
    public static final void enable(BalsaApplication application)
    {
        // view loaders
        application.getViewEngine().addSource(new FileViewSource(BalsaViewSource.Formats.APT, BalsaViewSource.Charsets.UTF8, "apt"));
        application.getViewEngine().addParser(BalsaViewSource.Formats.APT, new APTBalsaViewParser());
    }
}
