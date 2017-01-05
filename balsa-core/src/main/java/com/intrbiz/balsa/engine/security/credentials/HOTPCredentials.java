package com.intrbiz.balsa.engine.security.credentials;

/**
 * A HOTP based second factor authentication code 
 */
public interface HOTPCredentials extends Credentials
{
    /**
     * The HOTP authentication code
     */
    int getAuthenticationCode();
    
    public static class Simple implements HOTPCredentials
    {
        private int authenticationCode;
    
        public Simple()
        {
            super();
        }
    
        public Simple(int authenticationCode)
        {
            super();
            this.authenticationCode = authenticationCode;
        }
    
        @Override
        public int getAuthenticationCode()
        {
            return authenticationCode;
        }
    
        public void setAuthenticationCode(int authenticationCode)
        {
            this.authenticationCode = authenticationCode;
        }
    
        @Override
        public void release()
        {
            this.authenticationCode = -1;
        }
    }
}
