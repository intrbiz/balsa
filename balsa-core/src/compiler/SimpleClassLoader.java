package com.intrbiz.balsa.engine.route.impl.exec.compiler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

public class SimpleClassLoader extends ClassLoader
{
    private final JavaFileManager manager;

    private final Map<String, Class<?>> cache = new TreeMap<String, Class<?>>();

    public SimpleClassLoader(ClassLoader parent, JavaFileManager manager)
    {
        super(parent);
        this.manager = manager;
    }
    
    public boolean containsClass(String name)
    {
        return this.cache.containsKey(name);
    }

    @Override
    protected synchronized Class<?> findClass(String name) throws ClassNotFoundException
    {
        // check the class
        Class<?> cls = this.cache.get(name);
        if (cls != null) return cls;
        // find the class in the manager
        try
        {
            JavaFileObject jfo = this.manager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, name, Kind.CLASS, null);
            if (jfo == null) throw new ClassNotFoundException();
            //
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int l;
            try (InputStream in = jfo.openInputStream())
            {
                while ((l = in.read(b)) != -1)
                {
                    baos.write(b, 0, l);
                }
            }
            byte[] classData = baos.toByteArray();
            cls = this.defineClass(name, classData, 0, classData.length);
            this.cache.put(name, cls);
            return cls;
        }
        catch (ClassNotFoundException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ClassNotFoundException();
        }
    }
}
