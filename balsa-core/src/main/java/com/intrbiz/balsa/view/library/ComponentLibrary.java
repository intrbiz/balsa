package com.intrbiz.balsa.view.library;

import java.util.List;
import java.util.Set;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.parser.PostProcessor;

public interface ComponentLibrary
{
    public List<PostProcessor> postprocessors();
    public Component load(String name, String id) throws BalsaException;
    public Set<String> componentNames();
    public Class<? extends Component> componentClass(String name);
    
    String getName();
    String getLicense();
    String getDescription();
    String getInfo();
}
