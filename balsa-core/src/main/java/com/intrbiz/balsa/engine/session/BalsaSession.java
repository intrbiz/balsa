package com.intrbiz.balsa.engine.session;

import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.task.BalsaTaskState;

public interface BalsaSession
{   
    public static final String COOKIE_NAME = "BalsaSession";
    
    default Object getEntity(String name)
    {
        // try a var first
        Object value = this.var(name);
        if (value != null) return value;
        // next try a model
        value = this.model(name);
        return value;
    }
    
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
     * Get the state of the task with the given id
     */
    default BalsaTaskState task(String id)
    {
        return this.var(this.taskKey(id));
    }
    
    /**
     * Remove the state of a task
     */
    default void removeTask(String id)
    {
        this.removeVar(this.taskKey(id));
    }
    
    /**
     * Remove the state of a task if it is complete
     */
    default BalsaTaskState removeTaskIfComplete(String id)
    {
        final String key = this.taskKey(id);
        BalsaTaskState state = this.var(key);
        if (state != null && state.isComplete())
        {
            this.removeVar(key);
        }
        return state;
    }
    
    /**
     * Store the state of the given task in this session.
     * Tasks are stored as vars using a prefixed key.
     */
    default void task(String id, BalsaTaskState state)
    {
        this.var(this.taskKey(id), state);
    }
    
    /**
     * Get the key for a given task id
     */
    default String taskKey(String id)
    {
        return "balsa.task." + id;
    }
    
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
     * Get the authentication state for this session
     */
    AuthenticationState authenticationState();
}
