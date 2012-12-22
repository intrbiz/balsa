package com.intrbiz.balsa.metrics;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.metrics.collector.SessionMetricsCollector;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;

public class BalsaMetrics
{
    public static final MetricsRegistry REGISTRY = Metrics.defaultRegistry();
    
    /**
     * Instrument the given Balsa application
     */
    public static void instrument(BalsaApplication application)
    {
        // session metrics
        application.sessionEventListener(new SessionMetricsCollector(REGISTRY));
    }
}
