package com.intrbiz.balsa.engine;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.publicresource.PublicResource;

/**
 * Translate the given relative path into the URL for the public resource.
 */
public interface PublicResourceEngine extends BalsaEngine
{   
    /**
     * Translate the given relative path into the URL for the public resource.
     * 
     * The resulting URL maybe an absolute server path or an absolute URL.
     * 
     * 
     * 
     * @param path
     *            the relative path to the public resource
     * @return the URL to the resource.
     */
    String pub(BalsaContext context, String path);
    
    
    /**
     * Introspect a public resource
     * @param context
     * @param path the resource path
     * @return the resource metadata
     */
    PublicResource get(BalsaContext context, String path);
}
