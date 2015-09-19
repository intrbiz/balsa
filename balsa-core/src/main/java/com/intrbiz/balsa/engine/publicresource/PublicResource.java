package com.intrbiz.balsa.engine.publicresource;

import java.util.List;

public interface PublicResource
{
    String getPath();
    
    String getName();
    
    boolean exists();
    
    List<PublicResource> getChildren();
    
    PublicResource getParent();
}
