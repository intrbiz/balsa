package com.intrbiz.balsa.engine.security.credentials;

import com.intrbiz.balsa.error.BalsaSecurityException;
import com.yubico.u2f.data.messages.SignRequestData;
import com.yubico.u2f.data.messages.SignResponse;
import com.yubico.u2f.exceptions.U2fBadInputException;

/**
 * A U2F authentication response for a given challenge
 */
public class U2FAuthenticationChallengeResponse implements AuthenticationChallengeResponse
{
    private final SignRequestData challenge;
    
    private final SignResponse response;

    public U2FAuthenticationChallengeResponse(SignRequestData challenge, SignResponse response)
    {
        super();
        this.challenge = challenge;
        this.response = response;
    }
    
    public U2FAuthenticationChallengeResponse(String challenge, String response)
    {
        super();
        try
        {
            this.challenge = SignRequestData.fromJson(challenge);
            this.response = SignResponse.fromJson(response);
        }
        catch (U2fBadInputException e)
        {
            throw new BalsaSecurityException("Failed to decode U2F data", e);
        }
    }

    public SignRequestData getChallenge()
    {
        return challenge;
    }

    public SignResponse getResponse()
    {
        return response;
    }

    @Override
    public void release()
    {
    }
}
