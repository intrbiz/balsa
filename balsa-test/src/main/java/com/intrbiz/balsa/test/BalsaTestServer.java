package com.intrbiz.balsa.test;


import java.io.IOException;
import java.net.ServerSocket;
import java.security.SecureRandom;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.scgi.SCGIClient;

/**
 * Create, configure and start a Balsa application so that 
 * tests can be executed against it.
 * 
 * A port which is not in use will be chosen for the 
 * application to listen on.
 * 
 * The view path defaults to './src/main/views/' this can 
 * be set using the system property 'balsa.views'.
 * 
 */
public class BalsaTestServer<T extends BalsaApplication> implements TestRule
{
    private final Class<T> appClass;
    
    private Level logLevel = Level.TRACE;
    
    private T application;
    
    private int port = 0;
    
    public BalsaTestServer(Class<T> appClass)
    {
        super();
        this.appClass = appClass;
        this.port = this.randomPort();
    }
    
    public Class<? extends BalsaApplication> getAppClass()
    {
        return this.appClass;
    }
    
    /**
     * Set the Log4J log level
     * @param level
     * @return
     */
    public BalsaTestServer<T> logLevel(Level level)
    {
        this.logLevel = level;
        return this;
    }
    
    /**
     * Get the started application
     * @return
     */
    public T app()
    {
        return this.application;
    }
    
    /**
     * Get the port number the application is listening on
     * @return
     */
    public int port()
    {
        return this.port;
    }
    
    /**
     * Get a client which can connect to this application
     * @return
     */
    public SCGIClient client()
    {
        return new SCGIClient("127.0.0.1", this.port);
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement(){
            @Override
            public void evaluate() throws Throwable
            {
                startup();
                try
                {
                    base.evaluate();
                }
                finally
                {
                    shutdown();
                }
            }
        };
    }
    
    protected void startup() throws Exception
    {
        // logging?
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(this.logLevel);
        // set the port
        System.setProperty("balsa.scgi.port", String.valueOf(this.port));
        // start the application
        this.application = this.appClass.newInstance();
        this.application.start();
    }
    
    protected void shutdown() throws Exception
    {
        if (this.application != null)
        {
            this.application.stop();
        }
    }
    
    // helpers
    
    protected int choosePort()
    {
        for (int i = 0; i < 25; i++)
        {
            int port = this.randomPort();
            try
            {
                try (ServerSocket s = new ServerSocket(port))
                {
                    return port;
                }
            }
            catch (IOException e)
            {
            }
        }
        throw new RuntimeException("Failed to choose random port to launch the application on");
    }
    
    protected int randomPort()
    {
        return 8090 + new SecureRandom().nextInt(5000);
    }
}
