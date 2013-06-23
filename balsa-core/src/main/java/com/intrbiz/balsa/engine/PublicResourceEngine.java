package com.intrbiz.balsa.engine;

import com.intrbiz.balsa.BalsaContext;

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
}
