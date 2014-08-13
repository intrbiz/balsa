package com.intrbiz.balsa.engine.view;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class ViewMetadata
{
    private final Map<String, String> metadata = new TreeMap<String, String>();
    
    public ViewMetadata()
    {
        super();
    }
    
    public String getAttribute(String name)
    {
        return this.metadata.get(name.toLowerCase());
    }
    
    public void setAttribute(String name, String value)
    {
        this.metadata.put(name.toLowerCase(), value);
    }
    
    public boolean containsAttribute(String name)
    {
        return this.metadata.containsKey(name.toLowerCase());
    }
    
    public Set<String> getAttributeNames()
    {
        return this.metadata.keySet();
    }
    
    public void removeAttribute(String name)
    {
        this.metadata.remove(name.toLowerCase());
    }
}
