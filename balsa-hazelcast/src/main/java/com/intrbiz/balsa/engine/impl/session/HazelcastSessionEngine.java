package com.intrbiz.balsa.engine.impl.session;

import java.util.Set;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.intrbiz.balsa.BalsaException;

public class HazelcastSessionEngine extends AbstractSessionEngine
{
    private Config hazelcastConfig;

    private HazelcastInstance hazelcastInstance;

    private IMap<String, HazelcastSession> sessionMap;

    private IMap<String, Object> attributeMap;
    
    private Logger logger = Logger.getLogger(HazelcastSessionEngine.class);

    public HazelcastSessionEngine()
    {
        super();
    }
    
    public HazelcastSessionEngine(HazelcastInstance hazelcastInstance)
    {
        super();
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public String getEngineName()
    {
        return "Hazelcast-Balsa-Session-Engine";
    }
    
    public HazelcastInstance getHazelcastInstance()
    {
        return this.hazelcastInstance;
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

    @Override
    public void start() throws BalsaException
    {
        super.start();
        try
        {
            if (this.hazelcastInstance == null)
            {
                // setup hazelcast
                String hazelcastConfigFile = System.getProperty("hazelcast.config");
                if (hazelcastConfigFile != null)
                {
                    // when using a config file, you must configure the balsa.sessions map
                    this.hazelcastConfig = new XmlConfigBuilder(hazelcastConfigFile).build();
                }
                else
                {
                    // setup the default configuration
                    this.hazelcastConfig = new Config();
                    // add update configuration for our maps
                    MapConfig sessionMapConfig = this.hazelcastConfig.getMapConfig("balsa.sessions");
                    // session lifetime is in minutes
                    sessionMapConfig.setMaxIdleSeconds(this.getSessionLifetime() * 60);
                    sessionMapConfig.setEvictionPolicy(EvictionPolicy.LRU);
                    // default to storing objects, as with sticky balancing 
                    // requests to tend to the same server
                    sessionMapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
                    this.hazelcastConfig.addMapConfig(sessionMapConfig);
                }
                // set the instance name
                this.hazelcastConfig.setInstanceName(this.getBalsaApplication().getInstanceName());
                // create the instance
                this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(this.hazelcastConfig);
            }
            // create the maps
            this.sessionMap = this.hazelcastInstance.getMap("balsa.sessions");
            this.attributeMap = this.hazelcastInstance.getMap("balsa.sessions.attributes");
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
