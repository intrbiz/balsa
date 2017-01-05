package com.intrbiz.balsa.engine.security.info;

import java.io.Serializable;

import com.yubico.u2f.data.DeviceRegistration;

public class U2FAuthenticationDetail implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final DeviceRegistration authenticationDevice;

    public U2FAuthenticationDetail(DeviceRegistration authenticationDevice)
    {
        this.authenticationDevice = authenticationDevice;
    }

    public DeviceRegistration getAuthenticationDevice()
    {
        return authenticationDevice;
    }
}
