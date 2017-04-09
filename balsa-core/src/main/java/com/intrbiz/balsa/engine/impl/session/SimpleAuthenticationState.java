package com.intrbiz.balsa.engine.impl.session;

import java.security.Principal;
import java.util.Map;

import com.intrbiz.balsa.engine.security.AuthenticationResponse;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;

public class SimpleAuthenticationState implements AuthenticationState
{
    private Principal authenticating;
    
    private Principal current;
    
    private long authenticationStart = -1;
    
    private Map<String, AuthenticationChallenge> challenges;
    
    private AuthenticationInfo info;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Principal> T authenticatingPrincipal()
    {
        return (T) this.authenticating;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Principal> T currentPrincipal()
    {
        return (T) this.current;
    }

    @Override
    public long authenticationStartedAt()
    {
        return this.authenticationStart;
    }

    @Override
    public AuthenticationResponse update(AuthenticationResponse response)
    {
        if (response.isComplete())
        {
            this.challenges = null;
            this.authenticating = null;
            this.authenticationStart = -1L;
            this.info = response.getInfo();
            this.current = response.getPrincipal();
        }
        else
        {
            this.current = null;
            this.info = response.getInfo();
            this.challenges = response.getChallenges();
            this.authenticating = response.getPrincipal();
            this.authenticationStart = System.currentTimeMillis();
        }
        return response;
    }

    @Override
    public AuthenticationState reset()
    {
        this.current = null;
        this.authenticating = null;
        this.authenticationStart = -1L;
        this.info = null;
        this.challenges = null;
        return this;
    }

    @Override
    public AuthenticationInfo info()
    {
        return this.info;
    }

    @Override
    public Map<String, AuthenticationChallenge> challenges()
    {
        return this.challenges;
    }
}
