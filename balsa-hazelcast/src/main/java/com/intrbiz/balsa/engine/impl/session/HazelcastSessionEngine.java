package com.intrbiz.balsa.engine.impl.session;

import java.util.Set;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.impl.util.DefaultHazelcastFactory;

public class HazelcastSessionEngine extends AbstractSessionEngine
{
    public static final String BALSA_SESSION_MAP_NAME = "balsa.sessions";
    
    public static final String BALSA_SESSION_ATTRIBUTE_MAP_NAME = "balsa.sessions.attributes";
    
    private final Function<String, HazelcastInstance> hazelcastInstanceSupplier;
    
    private HazelcastInstance hazelcastInstance;

    private IMap<String, HazelcastSession> sessionMap;

    private IMap<String, Object> attributeMap;
    
    private Logger logger = Logger.getLogger(HazelcastSessionEngine.class);

    public HazelcastSessionEngine(Function<String, HazelcastInstance> hazelcastInstanceSupplier)
    {
        super();
        this.hazelcastInstanceSupplier = hazelcastInstanceSupplier;
    }
    
    public HazelcastSessionEngine(HazelcastInstance hazelcastInstance)
    {
        this((instanceName) -> hazelcastInstance);
    }
    
    public HazelcastSessionEngine()
    {
        this(new DefaultHazelcastFactory());
    }

    @Override
    public String getEngineName()
    {
        return "Hazelcast-Balsa-Session-Engine";
    }
    
    private void applyHazelcastConfiguration(Config hazelcastConfig)
    {
        // inject our configuration
        // add update configuration for our maps
        MapConfig sessionMapConfig = hazelcastConfig.getMapConfig(BALSA_SESSION_MAP_NAME);
        // session lifetime is in minutes
        sessionMapConfig.setMaxIdleSeconds(this.getSessionLifetime() * 60);
        sessionMapConfig.setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setSize(10).setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE));
        // default to storing objects, as with sticky balancing 
        // requests tend to the same server
        sessionMapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        hazelcastConfig.addMapConfig(sessionMapConfig);
        // setup the attribute map
        MapConfig sessionAttrMapConfig = hazelcastConfig.getMapConfig(BALSA_SESSION_ATTRIBUTE_MAP_NAME);
        // default to storing objects, as with sticky balancing 
        // requests tend to the same server
        sessionAttrMapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        hazelcastConfig.addMapConfig(sessionAttrMapConfig);
    }

    @Override
    public void start() throws BalsaException
    {
        super.start();
        try
        {
            // Get our hazelcast instance
            this.hazelcastInstance = this.hazelcastInstanceSupplier.apply(this.getBalsaApplication().getInstanceName());
            // Inject our HZ config
            this.applyHazelcastConfiguration(this.hazelcastInstance.getConfig());
            // create the maps
            this.sessionMap   = this.hazelcastInstance.getMap(BALSA_SESSION_MAP_NAME);
            this.attributeMap = this.hazelcastInstance.getMap(BALSA_SESSION_ATTRIBUTE_MAP_NAME);
            // eviction listener
            // this will remove attributes when a session is removed / evicted
            this.sessionMap.addLocalEntryListener(new EntryAddedListener<String, HazelcastSession>() {
            	@Override
                public void entryAdded(EntryEvent<String, HazelcastSession> event)
                {
                    logger.debug("Adding session: " + event.getKey());
                }
            });
            this.sessionMap.addLocalEntryListener(new EntryRemovedListener<String, HazelcastSession>() {
            	@Override
                public void entryRemoved(EntryEvent<String, HazelcastSession> event)
                {
                    // evict the attributes
                    logger.debug("Processing eviction of session: " + event.getKey());
                    logger.trace("Removing attributes of session: " + event.getKey());
                    Set<String> attributeKeys = attributeMap.keySet(new SessionPrefixPredicate(event.getKey()));
                    for (String key : attributeKeys)
                    {
                        logger.trace("Removing session attribute: " + key);
                        attributeMap.remove(key);
                    }
                }
            });
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to start Hazelcast Session Engine", e);
        }
    }
    
    @Override
    public HazelcastSession getSession(String sessionId)
    {
        HazelcastSession session = this.getSessionMap().get(sessionId);
        if (session == null)
        {
            session = new HazelcastSession(sessionId);
            this.getSessionMap().put(session.id(), session);
        }
        return session;
    }

    IMap<String, HazelcastSession> getSessionMap()
    {
        return this.sessionMap;
    }

    IMap<String, Object> getAttributeMap()
    {
        return this.attributeMap;
    }

    @Override
    public void stop()
    {
        super.stop();
    }
}
