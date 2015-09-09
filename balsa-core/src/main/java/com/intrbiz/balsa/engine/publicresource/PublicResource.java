package com.intrbiz.balsa.engine.publicresource;

import java.util.List;

public interface PublicResource
{
    String getPath();
    
    String getName();
    
    List<PublicResource> getChildren();
}
