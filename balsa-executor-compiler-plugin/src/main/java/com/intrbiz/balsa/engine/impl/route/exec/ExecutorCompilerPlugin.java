package com.intrbiz.balsa.engine.impl.route.exec;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Compile balsa route executors at compile time
 * 
 * @goal balsa-executor
 * @phase process-classes
 * @requiresProject
 * @requiresDependencyResolution runtime
 */
public class ExecutorCompilerPlugin extends AbstractMojo
{
    /**
     * @parameter expression="${project.basedir}"
     * @required
     */
    private File baseDirectory;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String artifactId;

    /**
     * @parameter expression="${project.version}"
     * @required
     */
    private String version;

    /**
     * @parameter default-value="${project.artifacts}"
     * @required
     * @readonly
     */
    private Collection<Artifact> artifacts;
    
    /**
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> compilePath;
    
    /**
     * @parameter
     * @required
     * @readonly
     */
    private String appClass;

    public ExecutorCompilerPlugin()
    {
        super();
    }

    public void execute() throws MojoExecutionException
    {
        Log log = this.getLog();
        try
        {
            Set<URL> urls = new HashSet<URL>();
            for (String ce : this.getCompilePath())
            {
                log.info("Adding compile path element: " + ce);
                urls.add(new File(ce).toURI().toURL());
            }
            // set the compiler target directory
            System.getProperties().setProperty("intrbiz.runtime.target", this.getClassesDirectory().getAbsolutePath());
            System.getProperties().setProperty("com.intrbiz.compiler.source", "true");
            // invoke the compiler
            try (URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[0])))
            {
                // load the application class
                Class<?> balsaAppClass = ucl.loadClass(this.getAppClass());
                Method setupRouters = balsaAppClass.getDeclaredMethod("setupRouters", new Class<?>[0]);
                setupRouters.setAccessible(true);
                Method getRouteEngine = balsaAppClass.getMethod("getRoutingEngine", new Class<?>[0]);
                Method toString = Object.class.getMethod("toString", new Class<?>[0]);
                // create te app
                Object balsaApp = balsaAppClass.newInstance();
                // invoke setup routers
                setupRouters.invoke(balsaApp, new Object[0]);
                // get the route engine
                Object routeEngine = getRouteEngine.invoke(balsaApp, new Object[0]);
                // info
                String routes = (String) toString.invoke(routeEngine);
                log.info("Compiled routes:\n" + routes);
            }
        }
        catch (Exception e)
        {
            log.error("Error compiling executors", e);
            throw new MojoExecutionException("Failed to compile executors", e);
        }
    }

    public File getBaseDirectory()
    {
        return baseDirectory;
    }

    public void setBaseDirectory(File baseDirectory)
    {
        this.baseDirectory = baseDirectory;
    }

    public File getClassesDirectory()
    {
        return classesDirectory;
    }

    public void setClassesDirectory(File classesDirectory)
    {
        this.classesDirectory = classesDirectory;
    }

    public File getTargetDirectory()
    {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory)
    {
        this.targetDirectory = targetDirectory;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Collection<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts(Collection<Artifact> artifacts)
    {
        this.artifacts = artifacts;
    }

    public List<String> getCompilePath()
    {
        return compilePath;
    }

    public void setCompilePath(List<String> compilePath)
    {
        this.compilePath = compilePath;
    }

    public String getAppClass()
    {
        return appClass;
    }

    public void setAppClass(String appClass)
    {
        this.appClass = appClass;
    }
}
