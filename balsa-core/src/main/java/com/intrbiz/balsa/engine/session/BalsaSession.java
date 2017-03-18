package com.intrbiz.balsa.engine.session;

import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.task.BalsaTaskState;

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
     * Get the named session variable
     * @param name the variable name
     * @return
     * returns Object
     */
    <T> T getVar(String name);
    
    /**
     * Store a variable in the session
     * @param name the variable name
     * @param object the variable
     * returns void
     */
    <T> T putVar(String name, T object);
    
    /**
     * Get the state of the task with the given id
     */
    default BalsaTaskState task(String id)
    {
        return this.getVar(this.taskKey(id));
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
        BalsaTaskState state = this.getVar(key);
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
        this.putVar(this.taskKey(id), state);
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
     * Store a model in this session
     * @param name the model name
     * @param model the model to store
     * @return the model
     */
    <T> T putModel(String name, T model);
    
    /**
     * Get a model
     * @param name the model name
     * @return the model
     */
    <T> T getModel(String name);
    
    /**
     * Remove a model from this session
     * @param name the model name
     */
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
