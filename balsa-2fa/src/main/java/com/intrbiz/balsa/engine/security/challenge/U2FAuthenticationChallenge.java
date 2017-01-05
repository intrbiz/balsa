package com.intrbiz.balsa.engine.security.challenge;

import java.io.Serializable;

import com.intrbiz.balsa.engine.security.challenge.AuthenticationChallenge;
import com.yubico.u2f.data.messages.AuthenticateRequestData;

/**
 * A U2F authentication challenge to start the second factor authentication process
 */
public class U2FAuthenticationChallenge implements AuthenticationChallenge, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final AuthenticateRequestData challenge;

    public U2FAuthenticationChallenge(AuthenticateRequestData challenge)
    {
        super();
        this.challenge = challenge;
    }

    public AuthenticateRequestData getChallenge()
    {
        return challenge;
    }
}
