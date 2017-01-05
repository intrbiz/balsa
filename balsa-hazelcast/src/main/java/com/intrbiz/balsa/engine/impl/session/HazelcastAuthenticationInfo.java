package com.intrbiz.balsa.engine.impl.session;

import java.io.Serializable;

import com.intrbiz.balsa.engine.security.info.AuthenticationInfo;

public class HazelcastAuthenticationInfo implements AuthenticationInfo, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String primaryAuthenticationMethodName;
    
    private final Object primaryAuthenticationMethodDetail;
    
    private final String secondaryAuthenticationMethodName;
    
    private final Object secondaryAuthenticationMethodDetail;

    public HazelcastAuthenticationInfo(AuthenticationInfo info)
    {
        super();
        this.primaryAuthenticationMethodName = info.primaryAuthenticationMethodName();
        this.primaryAuthenticationMethodDetail = info.primaryAuthenticationMethodDetail();
        this.secondaryAuthenticationMethodName = info.secondaryAuthenticationMethodName();
        this.secondaryAuthenticationMethodDetail = info.secondaryAuthenticationMethodDetail();
    }

    @Override
    public String primaryAuthenticationMethodName()
    {
        return primaryAuthenticationMethodName;
    }

    @Override
    public Object primaryAuthenticationMethodDetail()
    {
        return primaryAuthenticationMethodDetail;
    }

    @Override
    public String secondaryAuthenticationMethodName()
    {
        return secondaryAuthenticationMethodName;
    }

    @Override
    public Object secondaryAuthenticationMethodDetail()
    {
        return secondaryAuthenticationMethodDetail;
    }
    
    public static AuthenticationInfo wrap(AuthenticationInfo info)
    {
        return info instanceof Serializable ? info : new HazelcastAuthenticationInfo(info);
    }
}
