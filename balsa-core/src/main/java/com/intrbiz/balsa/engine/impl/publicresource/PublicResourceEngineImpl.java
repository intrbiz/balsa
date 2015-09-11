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
        File resource = new File(docRoot, path);
        if (! isParentOf(docRoot, resource)) return null;
        return new FilePublicResource(docRoot, resource);
    }
    
    private boolean isParentOf(File parent, File child)
    {
        if (parent.equals(child)) return true;
        File childParent = child.getParentFile();
        if (childParent != null) return isParentOf(parent, childParent); 
        return false;
    }
    
    private static class FilePublicResource implements PublicResource
    {
        private final File root;
        
        private final File file;
        
        private FilePublicResource parent;
        
        public FilePublicResource(File root, File file, FilePublicResource parent)
        {
            this.root = root;
            this.file = file;
            this.parent = parent;
        }
        
        public FilePublicResource(File root, File file)
        {
            this(root, file, null);
        }

        @Override
        public String getPath()
        {
            return this.file.getPath().substring(this.root.getPath().length());
        }

        @Override
        public String getName()
        {
            return this.file.getName();
        }
        
        @Override
        public boolean exists()
        {
            return this.file.exists();
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
                    ret.add(new FilePublicResource(this.root, child, this));
                }
            }
            return ret;
        }
        
        @Override
        public PublicResource getParent()
        {
            if (this.parent == null)
            {
                if (! this.file.equals(this.root))
                {
                    this.parent = new FilePublicResource(this.root, this.file.getParentFile());
                }
            }
            return this.parent;
        }
    }
}
