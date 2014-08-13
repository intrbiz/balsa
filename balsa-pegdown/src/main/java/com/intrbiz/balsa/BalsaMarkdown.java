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
     * @param parseMetadata should any metadata be extract from the view
     * @param hideTitle should the title element be hidden
     */
    public static final void enable(BalsaApplication application, boolean parseMetadata, boolean hideTitle)
    {
        // view loaders
        application.getViewEngine().addSource(new FileViewSource(BalsaViewSource.Formats.MARKDOWN, BalsaViewSource.Charsets.UTF8, "md"));
        application.getViewEngine().addParser(BalsaViewSource.Formats.MARKDOWN, new PegdownViewParser(parseMetadata, hideTitle));
    }
    
    /**
     * Enable Markdown support for the given application
     * @param application
     * @param hideTitle should the title element be hidden
     */
    public static final void enable(BalsaApplication application, boolean hideTitle)
    {
        enable(application, true, hideTitle);
    }
    
    /**
     * Enable Markdown support for the given application
     * @param application
     */
    public static final void enable(BalsaApplication application)
    {
        enable(application, true, true);
    }
}
