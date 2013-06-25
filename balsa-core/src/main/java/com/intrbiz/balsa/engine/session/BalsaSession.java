package com.intrbiz.balsa.engine.session;

import java.security.Principal;

public interface BalsaSession
{   
    public static final String COOKIE_NAME = "BalsaSession";
    
    /**
     * The session id
     * @return
     * returns String
     */
    String id();
    
    /**
     * The last time the session was accessed (in Unix time)
     * @return
     * returns long
     */
    long lastAccess();
    
    /**
     * Mark access on the session
     */
    void access();
    
    /**
     * Get the named session variable
     * @param name the variable name
     * @return
     * returns Object
     */
    Object var(String name);
    
    /**
     * Get the named session variable of the given type
     * @param name the variable name
     * @param type the variable type
     * @return
     * returns T
     */
    <T> T var(String name, Class<T> type);
    
    /**
     * Store a variable in the session
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    void var(String name, Object object);
    
    void removeVar(String name);
    
    /**
     * Create the session model of the given name
     * 
     * @param name
     *            the model name
     * @param type
     *            the model class
     * @return returns Object the model
     */
    <T> T model(String name, Class<T> type);
    <T> T model(String name, Class<T> type, boolean create);
    <T> T model(String name, T model);
    
    void removeModel(String name);
    
    /**
     * Deactivate the session
     * 
     * returns void
     */
    void deactivate();
    
    // security stuff
    
    /**
     * Get the currently authenticated principal for this session
     * @return
     */
    Principal currentPrincipal();
    
    /**
     * Store the currently authenticated principal
     * 
     * Note: This should only be called by BalsaContext.authenticate().
     * If your using this method directly, your doing it wrong.
     * 
     * @param principal
     */
    void setCurrentPrincipal(Principal principal);
    
    
    /**
     * The nonce used to validate requests, to mitigate request forgery.
     * @return
     */
    byte[] requestToken();
    
    String encodedRequestToken();
    
    void setRequestToken(byte[] token);
}
