package com.intrbiz.balsa.engine;

import java.util.List;
import java.util.Map;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.engine.view.BalsaViewParser;
import com.intrbiz.balsa.engine.view.BalsaViewRewriteRule;
import com.intrbiz.balsa.engine.view.BalsaViewSource;

/**
 * An engine to provide view processing to the Balsa Framework
 */
public interface ViewEngine extends BalsaEngine
{
    /**
     * Construct a view from the template names provided
     * 
     * @param names
     * @return returns View
     */
    BalsaView load(String[][] templates, String[] views, BalsaContext context) throws BalsaException;

    /**
     * Does the engine cache views?
     * 
     * @return returns boolean
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

    
    void clearSources();

    void removeSource(BalsaViewSource source);

    void addSource(BalsaViewSource source);

    List<BalsaViewSource> getSources();

    
    void clearParsers();

    void removeParser(String format);

    void addParser(String format, BalsaViewParser parser);

    Map<String, BalsaViewParser> getParsers();
    
    
    void clearRewriteRules();
    
    void removeRewriteRule(BalsaViewRewriteRule rule);
    
    void addRewriteRule(BalsaViewRewriteRule rule);
    
    List<BalsaViewRewriteRule> getRewriteRules();
}
