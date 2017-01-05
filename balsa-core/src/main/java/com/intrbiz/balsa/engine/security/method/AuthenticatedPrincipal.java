package com.intrbiz.balsa.engine.security.method;

import java.security.Principal;

public class AuthenticatedPrincipal
{
    private final Principal principal;
    
    private final String authenticationMethod;
    
    private final Object authenticationInfoDetail;

    public AuthenticatedPrincipal(Principal principal, String authenticationMethod, Object authenticationInfoDetail)
    {
        super();
        this.principal = principal;
        this.authenticationMethod = authenticationMethod;
        this.authenticationInfoDetail = authenticationInfoDetail;
    }
    
    public AuthenticatedPrincipal(Principal principal, String authenticationMethod)
    {
        this(principal, authenticationMethod, null);
    }

    public Principal getPrincipal()
    {
        return principal;
    }

    public String getAuthenticationMethod()
    {
        return authenticationMethod;
    }

    public Object getAuthenticationInfoDetail()
    {
        return authenticationInfoDetail;
    }
}
