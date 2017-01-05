package com.intrbiz.balsa.engine.security.info;

import java.io.Serializable;

import com.intrbiz.util.HOTPRegistration;

public class HOTPAuthenticationDetail implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final HOTPRegistration hotp;
    
    public HOTPAuthenticationDetail(HOTPRegistration hotp)
    {
        super();
        this.hotp = hotp;
    }

    public HOTPRegistration getHotp()
    {
        return hotp;
    }
}
