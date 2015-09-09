package com.intrbiz.balsa.engine.impl.publicresource;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.PublicResourceEngine;
import com.intrbiz.balsa.engine.impl.AbstractBalsaEngine;
import com.intrbiz.balsa.engine.publicresource.PublicResource;

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

    @Override
    public PublicResource get(BalsaContext context, String path)
    {
        File docRoot = new File(context.request().getDocumentRoot());
        System.out.println("Document Root: " + docRoot);
        File resource = new File(docRoot, path);
        return new FilePublicResource(resource);
    }
    
    private static class FilePublicResource implements PublicResource
    {
        private final File file;
        
        public FilePublicResource(File file)
        {
            this.file = file;
        }

        @Override
        public String getPath()
        {
            return this.file.getPath();
        }

        @Override
        public String getName()
        {
            return this.file.getName();
        }

        @Override
        public List<PublicResource> getChildren()
        {
            List<PublicResource> ret = new LinkedList<PublicResource>();
            File[] files = this.file.listFiles();
            if (files != null)
            {
                for (File child : files)
                {
                    ret.add(new FilePublicResource(child));
                }
            }
            return ret;
        }
    }
}
