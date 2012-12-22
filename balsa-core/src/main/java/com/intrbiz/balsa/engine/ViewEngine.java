package com.intrbiz.balsa.engine;

import java.io.File;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;

/**
 * An engine to provide view processing to the Balsa Framework
 */
public interface ViewEngine extends BalsaEngine
{
    /**
     * Construct a view from the template names provided
     * @param names
     * @return
     * returns View
     */
    BalsaView load(String[] names, boolean useTemplate, BalsaContext context) throws BalsaException;
    
    /**
     * Does the engine cache views?
     * @return
     * returns boolean
     */
    boolean isCache();
    
    /**
     * The engine should cache views (Default)
     */
    void cacheOn();
    
    /**
     * The engine should not cache views
     */
    void cacheOff();
    
    /**
     * Get the template views
     * @return
     * returns String[]
     */
    String[] getTemplates();
    
    /**
     * Add a template view to be used
     * @param template
     * returns void
     */
    void template(String template);
    
    /**
     * Set the template views to be used
     * @param templates
     * returns void
     */
    void templates(String[] templates);
    
    /**
     * Get the base directory for views
     * @return
     * returns File
     */
    File getBase();
    
    /**
     * Set the base directory for views
     * @param base
     * returns void
     */
    void base(File base);
}
