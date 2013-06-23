package com.intrbiz.balsa.engine.impl.publicresource;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.PublicResourceEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;

public class PublicResourceEngineImpl extends AbstractBalsaEngine implements PublicResourceEngine
{
    public PublicResourceEngineImpl()
    {
        super();
    }

    @Override
    public String getEngineName()
    {
      return "Balsa-Public-Resource-Engine";
    }

    @Override
    public String pub(BalsaContext context, String path)
    {
        // translate to the absolute path
        return context.path(path);
    }
}
