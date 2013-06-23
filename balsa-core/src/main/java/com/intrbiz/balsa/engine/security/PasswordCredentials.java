package com.intrbiz.balsa.engine.security;

/**
 * A pairing of principal name (username) and a password
 */
public interface PasswordCredentials extends LoginCredentials
{
    /**
     * Get the password
     * @return
     */
    char[] getPassword();
    
    /**
     * A simple implementation of PasswordCredentials
     */
    public static class Simple implements PasswordCredentials
    {
        private String username;
        
        private String password;
        
        public Simple(String username, String password)
        {
            this.username = username;
            this.password = password;
        }
        
        @Override
        public String getPrincipalName()
        {
            return this.username;
        }

        @Override
        public void release()
        {
            this.password = null;
        }

        @Override
        public char[] getPassword()
        {
            return this.password == null ? null : this.password.toCharArray();
        }
    }
}
