package com.intrbiz.balsa.engine.impl.util;

import java.util.function.Function;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaException;

public class DefaultHazelcastFactory implements Function<String, HazelcastInstance>
{
    @Override
    public HazelcastInstance apply(String instanceName)
    {
        Config config = this.loadHazelcastConfig();
        config.setInstanceName(instanceName);
        return Hazelcast.getOrCreateHazelcastInstance(config);
    }
    
    private Config loadHazelcastConfig()
    {
        try
        {
            // setup hazelcast
            Config hazelcastConfig;
            String hazelcastConfigFile = Util.coalesceEmpty(System.getProperty("hazelcast.config"), System.getenv("hazelcast_config"));
            if (hazelcastConfigFile != null)
            {
                hazelcastConfig = new XmlConfigBuilder(hazelcastConfigFile).build();
            }
            else
            {
                hazelcastConfig = new Config();
            }
            // set the instance name
            return hazelcastConfig;
        }
        catch (Exception e)
        {
            throw new BalsaException("Failed to load Hazelcast configuration", e);
        }
    }
}
