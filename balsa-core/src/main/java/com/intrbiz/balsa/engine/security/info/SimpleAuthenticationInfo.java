package com.intrbiz.balsa.engine.security.info;

import java.io.Serializable;

/**
 * Basic authentication info implementation
 */
public class SimpleAuthenticationInfo implements AuthenticationInfo, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String primaryAuthenticationMethodName;
    
    private final Object primaryAuthenticationMethodDetail;
    
    private final String secondaryAuthenticationMethodName;
    
    private final Object secondaryAuthenticationMethodDetail;

    public SimpleAuthenticationInfo(String primaryAuthenticationMethodName, Object primaryAuthenticationMethodDetail, String secondaryAuthenticationMethodName, Object secondaryAuthenticationMethodDetail)
    {
        super();
        this.primaryAuthenticationMethodName = primaryAuthenticationMethodName;
        this.primaryAuthenticationMethodDetail = primaryAuthenticationMethodDetail;
        this.secondaryAuthenticationMethodName = secondaryAuthenticationMethodName;
        this.secondaryAuthenticationMethodDetail = secondaryAuthenticationMethodDetail;
    }
    
    public SimpleAuthenticationInfo(String primaryAuthenticationMethodName, Object primaryAuthenticationMethodDetail)
    {
        super();
        this.primaryAuthenticationMethodName = primaryAuthenticationMethodName;
        this.primaryAuthenticationMethodDetail = primaryAuthenticationMethodDetail;
        this.secondaryAuthenticationMethodName = null;
        this.secondaryAuthenticationMethodDetail = null;
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
}
