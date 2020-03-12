package com.intrbiz.balsa.engine.impl.session;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

import com.hazelcast.map.IMap;
import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.security.AuthenticationResponse;
import com.intrbiz.balsa.engine.security.AuthenticationState;
import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;
import com.intrbiz.balsa.engine.session.BalsaSession;

public class HazelcastSession implements BalsaSession, AuthenticationState, Serializable
{
    private static final long serialVersionUID = 1L;

    protected String sessionId;

    public HazelcastSession(final String id)
    {
        super();
        this.sessionId = id;
    }

    public HazelcastSession()
    {
        super();
    }

    @Override
    public final String id()
    {
        return this.sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    @Override
    public AuthenticationState authenticationState()
    {
        return this;
    }

    @Override
    public long authenticationStartedAt()
    {
        Long startedAt = (Long) this.getAttributeMap().get(this.authenticationStartedAtId());
        return startedAt == null ? -1L : startedAt.longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Principal> T authenticatingPrincipal()
    {
        return (T) this.getAttributeMap().get(this.authenticatingPrincipalId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, AuthenticationChallenge> challenges()
    {
        return (Map<String, AuthenticationChallenge>) this.getAttributeMap().get(this.authenticationChallengesId());
    }

    @Override
    public AuthenticationInfo info()
    {
        return (AuthenticationInfo) this.getAttributeMap().get(this.authenticationInfoId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Principal> T currentPrincipal()
    {
        return (T) this.getAttributeMap().get(this.principalId());
    }

    @Override
    public AuthenticationResponse update(AuthenticationResponse response)
    {
        if (response == null) throw new IllegalArgumentException("Response cannot be null");
        IMap<String, Object> attrs = this.getAttributeMap();
        if (response.isComplete())
        {
            attrs.remove(this.authenticationChallengesId());
            attrs.remove(this.authenticatingPrincipalId());
            attrs.remove(this.authenticationStartedAtId());
            attrs.put(this.authenticationInfoId(), HazelcastAuthenticationInfo.wrap(response.getInfo()));
            attrs.put(this.principalId(), response.getPrincipal());
        }
        else
        {
            attrs.remove(this.principalId());
            attrs.put(this.authenticationInfoId(), HazelcastAuthenticationInfo.wrap(response.getInfo()));
            attrs.put(this.authenticationChallengesId(), response.getChallenges());
            attrs.put(this.authenticatingPrincipalId(), response.getPrincipal());
            attrs.put(this.authenticationStartedAtId(), new Long(System.currentTimeMillis()));
        }
        return response;
    }

    @Override
    public AuthenticationState reset()
    {
        IMap<String, Object> attrs = this.getAttributeMap();
        attrs.remove(this.principalId());
        attrs.remove(this.authenticatingPrincipalId());
        attrs.remove(this.authenticationStartedAtId());
        attrs.remove(this.authenticationInfoId());
        attrs.remove(this.authenticationChallengesId());
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getVar(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return (T) this.getAttributeMap().get(this.varId(name));
    }

    @Override
    public <T> T putVar(String name, T object)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (object == null)
        {
            this.getAttributeMap().remove(this.varId(name));
        }
        else
        {
            this.getAttributeMap().put(this.varId(name), object);
        }
        return object;
    }

    @Override
    public void removeVar(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.getAttributeMap().remove(this.varId(name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getModel(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return (T) this.getAttributeMap().get(this.modelId(name));
    }

    @Override
    public <T> T putModel(String name, T model)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (model == null)
        {
            this.getAttributeMap().remove(this.modelId(name));
        }
        else
        {
            this.getAttributeMap().put(this.modelId(name), model);
        }
        return model;
    }

    @Override
    public void removeModel(String name)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        this.getAttributeMap().remove(this.modelId(name));
    }

    @Override
    public void deactivate()
    {
    }

    // util

    protected String varId(String name)
    {
        return this.id() + ".var." + name;
    }

    protected String modelId(String name)
    {
        return this.id() + ".model." + name;
    }
    
    protected String principalId()
    {
        return this.id() + ".principal";
    }
    
    protected String authenticatingPrincipalId()
    {
        return this.id() + ".authentication.principal";
    }
    
    protected String authenticationStartedAtId()
    {
        return this.id() + ".authentication.started.at";
    }
    
    protected String authenticationInfoId()
    {
        return this.id() + ".authentication.info";
    }
    
    protected String authenticationChallengesId()
    {
        return this.id() + ".authentication.challenges";
    }
    
    protected IMap<String, Object> getAttributeMap()
    {
        BalsaApplication app = BalsaApplication.getInstance();
        if (app == null) throw new RuntimeException("Failed to load BalsaApplication, cannot find session");
        return ((HazelcastSessionEngine) app.getSessionEngine()).getAttributeMap();
    }
}
