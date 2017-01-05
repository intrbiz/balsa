package com.intrbiz.balsa.engine.security.info;

import java.io.Serializable;

public class BackupCodeAuthenticationDetail implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final String backupCode;
    
    public BackupCodeAuthenticationDetail(String backupCode)
    {
        super();
        this.backupCode = backupCode;
    }

    public String getBackupCode()
    {
        return backupCode;
    }
}
