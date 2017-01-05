package com.intrbiz.balsa.engine.security.credentials;

/**
 * A backup code based second factor authentication code
 */
public interface BackupCodeCredentials extends Credentials
{
    /**
     * The backup authentication code
     */
    String getBackupCode();
    
    public static class Simple implements BackupCodeCredentials
    {
        private String backupCode;
    
        public Simple()
        {
            super();
        }
    
        public Simple(String backupCode)
        {
            super();
            this.backupCode = backupCode;
        }
    
        @Override
        public String getBackupCode()
        {
            return backupCode;
        }
    
        @Override
        public void release()
        {
            this.backupCode = null;
        }
    }
}
