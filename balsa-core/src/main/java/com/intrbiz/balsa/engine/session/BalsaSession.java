package com.intrbiz.balsa.engine.session;

import java.security.Principal;

public interface BalsaSession
{   
    public static final String COOKIE_NAME = "BalsaSession";
    
    Object getEntity(String name);
    
    /**
     * The session id
     * @return
     * returns String
     */
    String id();
    
    /**
     * Get the named session variable
     * @param name the variable name
     * @return
     * returns Object
     */
    <T> T var(String name);
    
    /**
     * Store a variable in the session
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    <T> T var(String name, T object);
    
    /**
     * Remove a variable of the given name
     * @param name
     */
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
    <T> T model(String name, Class<T> type, boolean create);
    <T> T model(String name, T model);
    <T> T model(String name);
    
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
    <T extends Principal> T currentPrincipal();
    
    /**
     * Store the currently authenticated principal
     * 
     * Note: This should only be called by BalsaContext.authenticate().
     * If your using this method directly, your doing it wrong.
     * 
     * @param principal
     */
    <T extends Principal> T currentPrincipal(T principal);
}
