package com.intrbiz.balsa.test.route.model;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermissions;
import com.intrbiz.metadata.RequireSession;
import com.intrbiz.metadata.RequireValidAccessToken;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidPrincipal;

@Prefix("/")
@RequireSession()
@RequireValidPrincipal()
public class RestrictedRouter extends Router<BalsaApplication>
{
    @Get("/restricted")
    @RequireValidPrincipal()
    public void restricted()
    {
        
    }
    
    @Get("/restricted/by/permission")
    @RequirePermissions("test.permission")
    public void restrictedByPermission()
    {
        
    }

    @Get("/restricted/by/permissions")
    @RequirePermissions({"test.permission", "another.permission"})
    public void restrictedByPermissions()
    {
        
    }
    
    @Get("/csrf/1")
    @RequireValidAccessToken()
    public void csrfCheck1()
    {
    }
    
    @Get("/csrf/2")
    @RequireValidAccessToken(@Param("token"))
    public void csrfCheck2()
    {
    }
    
    @Get("/csrf/3")
    @RequireValidAccessTokenForURL(value = @Param("token"))
    public void csrfCheck3()
    {
    }
    
    @Get("/csrf/4")
    @RequireValidAccessTokenForURL()
    @RequireSession()
    public void csrfCheck4()
    {
    }
}
