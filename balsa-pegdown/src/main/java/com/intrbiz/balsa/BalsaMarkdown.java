package com.intrbiz.balsa;

import com.intrbiz.balsa.engine.impl.view.FileViewSource;
import com.intrbiz.balsa.engine.view.BalsaViewSource;

/**
 * Syntactic Sugar
 */
public class BalsaMarkdown
{
    /**
     * Enable Markdown support for the given application
     * @param application
     */
    public static final void enable(BalsaApplication application)
    {
        // view loaders
        application.getViewEngine().addSource(new FileViewSource(BalsaViewSource.Formats.MARKDOWN, BalsaViewSource.Charsets.UTF8, "md"));
        application.getViewEngine().addParser(BalsaViewSource.Formats.MARKDOWN, new PegdownViewParser());
    }
}
