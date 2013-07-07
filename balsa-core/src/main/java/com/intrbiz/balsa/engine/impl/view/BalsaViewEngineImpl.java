package com.intrbiz.balsa.engine.impl.view;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.ViewEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.balsa.engine.view.BalsaViewParser;
import com.intrbiz.balsa.engine.view.BalsaViewRewriteRule;
import com.intrbiz.balsa.engine.view.BalsaViewSource;
import com.intrbiz.balsa.error.view.BalsaViewNotFound;
import com.intrbiz.cache.IBCache;
import com.intrbiz.cache.WeakCache;
import com.intrbiz.express.ExpressException;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class BalsaViewEngineImpl extends AbstractBalsaEngine implements ViewEngine
{
    private IBCache<String, BalsaView> cache = new WeakCache<String, BalsaView>();

    private Logger logger = Logger.getLogger(BalsaViewEngineImpl.class);

    private boolean cacheOn = true;

    private Timer sourceDuration = Metrics.newTimer(ViewEngine.class, "source-duration", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private Timer parseDuration = Metrics.newTimer(ViewEngine.class, "parse-duration", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private Timer loadDuration = Metrics.newTimer(ViewEngine.class, "load-duration", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    protected List<BalsaViewRewriteRule> rewriteRules = new CopyOnWriteArrayList<BalsaViewRewriteRule>();

    protected List<BalsaViewSource> sources = new CopyOnWriteArrayList<BalsaViewSource>();

    protected ConcurrentMap<String, BalsaViewParser> parsers = new ConcurrentHashMap<String, BalsaViewParser>();
    
    protected boolean viewMetrics = true;

    public BalsaViewEngineImpl(boolean viewMetrics)
    {
        super();
        this.viewMetrics = viewMetrics;
        // defaults
        this.addSource(new FileViewSource());
        this.addParser(BalsaViewSource.Formats.BALSA, new BalsaViewParserImpl());
    }
    
    public BalsaViewEngineImpl()
    {
        this(true);
    }

    protected BalsaViewSource.Resource source(String name, BalsaContext context) throws BalsaException
    {
        TimerContext tctx = this.sourceDuration.time();
        try
        {
            // simple linear search for the resource
            for (BalsaViewSource source : this.sources)
            {
                logger.trace("Trying source: " + source.getClass().getSimpleName());
                BalsaViewSource.Resource res = source.open(name, context);
                if (res != null) return res;
            }
            throw new BalsaViewNotFound(name);
        }
        finally
        {
            tctx.stop();
        }
    }

    protected BalsaView parse(BalsaView previous, BalsaViewSource.Resource resource, BalsaContext context) throws BalsaException
    {
        TimerContext tctx = this.parseDuration.time();
        try
        {
            BalsaViewParser parser = this.parsers.get(resource.getFormat());
            if (parser != null)
            {
                // parse it
                return parser.parse(previous, resource, context);
            }
            throw new BalsaException("Failed to find parser for " + resource.getFormat());
        }
        catch (ExpressException e)
        {
            throw new BalsaViewNotFound("Failed to parse view", e);
        }
        finally
        {
            tctx.stop();
        }
    }

    protected String rewriteView(String name)
    {
        for (BalsaViewRewriteRule rule : this.rewriteRules)
        {
            if (rule.accept(name))
            {
                name = rule.rewrite(name);
                // rules are chained by default
                if (rule.isLast()) return name;
            }
        }
        return name;
    }

    protected BalsaView loadView(BalsaView previous, String name, BalsaContext context) throws BalsaException
    {
        // process any rewrite rules
        name = this.rewriteView(name);
        // source the view
        BalsaViewSource.Resource resource = this.source(name, context);
        logger.trace("Loaded resource: " + name + " => " + resource.getFormat() + " " + resource.getName());
        // parse the view
        BalsaView view = this.parse(previous, resource, context);
        return view;
    }

    @Override
    public BalsaView load(String[][] templates, String[] names, BalsaContext context) throws BalsaException
    {
        if (names == null || names.length == 0) throw new BalsaException("Cannot load view, no names given");
        String key = key(templates, names);
        BalsaView head = this.cacheOn ? this.cache.get(key) : null;
        if (head != null) return head;
        //
        TimerContext tctx = this.loadDuration.time();
        try
        {
            BalsaView tail = null;
            if (templates != null && templates.length > 0)
            {
                for (String[] templateViews : templates)
                {
                    if (templateViews != null && templateViews.length > 0)
                    {
                        for (String templateView : templateViews)
                        {
                            if (this.logger.isTraceEnabled()) this.logger.trace("Parsing template view: " + templateView);
                            // load the view
                            tail = this.loadView(tail, templateView, context);
                            if (head == null) head = tail;
                        }
                    }
                }
            }
            for (String name : names)
            {
                this.logger.trace("Parsing view: " + name);
                tail = this.loadView(tail, name, context);
                if (head == null) head = tail;
            }
            if (this.cacheOn) this.cache.cachePrivate(key, head);
        }
        finally
        {
            tctx.stop();
        }
        // wrap with metrics?
        return this.viewMetrics ? new ViewMetricsWrapper(key, head) : head;
    }

    private static String key(String[][] templates, String[] names)
    {
        StringBuilder sb = new StringBuilder();
        boolean ns = false;
        if (templates != null)
        {
            for (String[] templateSet : templates)
            {
                if (templateSet != null)
                {
                    for (String template : templateSet)
                    {
                        if (ns) sb.append("; ");
                        sb.append(template);
                        ns = true;
                    }
                }
            }
        }
        for (String name : names)
        {
            if (ns) sb.append("; ");
            sb.append(name);
            ns = true;
        }
        return sb.toString();
    }

    @Override
    public boolean isCache()
    {
        return this.cacheOn;
    }

    @Override
    public void cacheOn()
    {
        this.cacheOn = true;
    }

    @Override
    public void cacheOff()
    {
        this.cacheOn = false;
        this.cache.clear();
        this.logger.warn("View caching has been disabled");
    }

    @Override
    public void clearSources()
    {
        this.sources.clear();
    }

    @Override
    public void removeSource(BalsaViewSource source)
    {
        this.sources.remove(source);
    }

    @Override
    public void addSource(BalsaViewSource source)
    {
        this.sources.add(source);
    }

    @Override
    public List<BalsaViewSource> getSources()
    {
        return Collections.unmodifiableList(this.sources);
    }

    @Override
    public void clearParsers()
    {
        this.parsers.clear();
    }

    @Override
    public void removeParser(String format)
    {
        this.parsers.remove(format);
    }

    @Override
    public void addParser(String format, BalsaViewParser parser)
    {
        this.parsers.put(format, parser);
    }

    @Override
    public Map<String, BalsaViewParser> getParsers()
    {
        return Collections.unmodifiableMap(this.parsers);
    }

    @Override
    public void clearRewriteRules()
    {
        this.rewriteRules.clear();
    }

    @Override
    public void removeRewriteRule(BalsaViewRewriteRule rule)
    {
        this.rewriteRules.remove(rule);
    }

    @Override
    public void addRewriteRule(BalsaViewRewriteRule rule)
    {
        this.rewriteRules.add(rule);
    }

    @Override
    public List<BalsaViewRewriteRule> getRewriteRules()
    {
        return Collections.unmodifiableList(this.rewriteRules);
    }

    @Override
    public String getEngineName()
    {
        return "Balsa-View-Engine";
    }
}
