package com.intrbiz.balsa.engine.security.credentials;

import com.intrbiz.balsa.engine.security.credentials.AuthenticationChallengeResponse;
import com.yubico.u2f.data.messages.AuthenticateRequestData;
import com.yubico.u2f.data.messages.AuthenticateResponse;

/**
 * A U2F authentication response for a given challenge
 */
public class U2FAuthenticationChallengeResponse implements AuthenticationChallengeResponse
{
    private final AuthenticateRequestData challenge;
    
    private final AuthenticateResponse response;

    public U2FAuthenticationChallengeResponse(AuthenticateRequestData challenge, AuthenticateResponse response)
    {
        super();
        this.challenge = challenge;
        this.response = response;
    }
    
    public U2FAuthenticationChallengeResponse(String challenge, String response)
    {
        super();
        this.challenge = AuthenticateRequestData.fromJson(challenge);
        this.response = AuthenticateResponse.fromJson(response);
    }

    public AuthenticateRequestData getChallenge()
    {
        return challenge;
    }

    public AuthenticateResponse getResponse()
    {
        return response;
    }

    @Override
    public void release()
    {
    }
}
