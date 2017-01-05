package com.intrbiz.balsa.engine.security;

import java.security.Principal;
import java.util.Map;

import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;

/**
 * The result of an authentication, either all complete or a second factor challenge is provided
 */
public class AuthenticationResponse
{
    private final Principal principal;
    
    private final boolean complete;
    
    private final AuthenticationInfo info;
    
    private final Map<String, AuthenticationChallenge> challenges;

    public AuthenticationResponse(Principal principal, boolean complete, AuthenticationInfo info, Map<String, AuthenticationChallenge> challenges)
    {
        super();
        this.principal = principal;
        this.complete = complete;
        this.info = info;
        this.challenges = challenges;
    }
    
    public AuthenticationResponse(Principal principal)
    {
        this(principal, true, null, null);
    }
    
    public AuthenticationResponse(Principal principal, AuthenticationInfo info)
    {
        this(principal, true, info, null);
    }

    /**
     * Get the authenticated principal
     */
    @SuppressWarnings("unchecked")
    public <T extends Principal> T getPrincipal()
    {
        return (T) principal;
    }

    /**
     * Is the authentication process complete, or is a second authentication factor required
     */
    public boolean isComplete()
    {
        return complete;
    }

    /**
     * The security engine specific authentication information
     */
    public AuthenticationInfo getInfo()
    {
        return info;
    }

    /**
     * The security engine specific authentication challenge required for second factor authentication
     */
    public Map<String, AuthenticationChallenge> getChallenges()
    {
        return challenges;
    }
}
