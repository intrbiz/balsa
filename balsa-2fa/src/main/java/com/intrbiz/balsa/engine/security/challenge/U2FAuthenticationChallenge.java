package com.intrbiz.balsa.engine.security.challenge;

import java.io.Serializable;

import com.yubico.u2f.data.messages.SignRequestData;

/**
 * A U2F authentication challenge to start the second factor authentication process
 */
public class U2FAuthenticationChallenge implements AuthenticationChallenge, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final SignRequestData challenge;

    public U2FAuthenticationChallenge(SignRequestData challenge)
    {
        super();
        this.challenge = challenge;
    }

    public SignRequestData getChallenge()
    {
        return challenge;
    }
}
