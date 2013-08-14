package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.impl.route.exec.model.ExecutorClass;
import com.intrbiz.balsa.engine.route.RouteExecutor;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Cookie;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Header;
import com.intrbiz.metadata.IsArgument;
import com.intrbiz.metadata.IsResponse;
import com.intrbiz.metadata.IsRouteWrapper;
import com.intrbiz.metadata.IsSecurityCheck;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermissions;
import com.intrbiz.metadata.RequireSession;
import com.intrbiz.metadata.RequireValidAccessToken;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.XML;
import com.intrbiz.util.compiler.CompilerTool;

public class ExecBuilder
{
    private static final AtomicInteger ID_SEQ = new AtomicInteger();
    
    private final int id = ID_SEQ.incrementAndGet();
    
    private int arity = 0;

    private int currentArgumentIndex = 0;

    private ArgumentBuilder<?>[] arguments;

    private Method handler;

    private Router router;

    private ResponseBuilder response = null;
    
    private List<SecurityBuilder> securityBuilders = new LinkedList<SecurityBuilder>();
    
    private List<RouteWrapperBuilder> wrapperBuilders = new LinkedList<RouteWrapperBuilder>();

    public ExecBuilder()
    {
        super();
    }
    
    public int getId()
    {
        return this.id;
    }

    public ExecBuilder handler(Method handler)
    {
        this.handler = handler;
        this.arity = handler.getParameterTypes().length;
        this.arguments = new ArgumentBuilder[this.arity];
        return this;
    }

    public ExecBuilder router(Router router)
    {
        this.router = router;
        return this;
    }

    public ExecBuilder argument(ArgumentBuilder<?> builder)
    {
        if (this.currentArgumentIndex >= this.arity) throw new IllegalStateException("To many arguments");
        builder.index(this.currentArgumentIndex);
        builder.parameterType(this.handler.getParameterTypes()[this.currentArgumentIndex]);
        this.arguments[this.currentArgumentIndex] = builder;
        this.currentArgumentIndex++;
        return this;
    }

    public ExecBuilder nullArgument()
    {
        return argument(new NullArgument());
    }

    public ExecBuilder parameterArgument(String name)
    {
        return argument(new ParameterArgument().name(name));
    }

    public ExecBuilder headerArgument(String name)
    {
        return argument(new HeaderArgument().name(name));
    }

    public ExecBuilder cookieArgument(String name)
    {
        return argument(new CookieArgument().name(name));
    }

    public ExecBuilder xmlArgument(Class<?> type) throws JAXBException
    {
        return argument(new XMLArgument().type(type));
    }

    public ExecBuilder xmlArgument() throws JAXBException
    {
        return xmlArgument(this.handler.getParameterTypes()[this.currentArgumentIndex]);
    }
    
    public ExecBuilder jsonArgument(Class<?> type) throws JAXBException
    {
        return argument(new JSONArgument().type(type));
    }

    public ExecBuilder jsonArgument() throws JAXBException
    {
        return xmlArgument(this.handler.getParameterTypes()[this.currentArgumentIndex]);
    }

    public ExecBuilder response(ResponseBuilder eb)
    {
        this.response = eb;
        return this;
    }

    public ExecBuilder xmlResponse(Class<?> type) throws JAXBException
    {
        return response(new XMLResponse().type(type));
    }

    public ExecBuilder xmlResponse() throws JAXBException
    {
        return xmlResponse(this.handler.getReturnType());
    }

    public ExecBuilder jsonResponse(Class<?> type) throws JAXBException
    {
        return response(new JSONResponse().type(type));
    }

    public ExecBuilder jsonResponse() throws JAXBException
    {
        return jsonResponse(this.handler.getReturnType());
    }
    
    public ExecBuilder securityCheck(SecurityBuilder builder)
    {
        // don't add duplicates
        if (builder.isSingular())
        {
            for (SecurityBuilder b : this.securityBuilders)
            {
                if (builder.getClass() == b.getClass()) 
                    return this;
            }
        }
        // add the builder
        this.securityBuilders.add(builder);
        return this;
    }
    
    public ExecBuilder validPrincipal()
    {
        return this.securityCheck(new ValidPrincipalBuilder());
    }
    
    public ExecBuilder permission(String permission)
    {
        return this.securityCheck(new PermissionsBuilder().permission(permission));
    }
    
    public ExecBuilder permission(String[] permissions)
    {
        return this.securityCheck(new PermissionsBuilder().permission(permissions));
    }
    
    public ExecBuilder validAccessToken(String parameterName)
    {
        this.securityCheck(new ValidAccessTokenBuilder(parameterName));
        return this;
    }
    
    public ExecBuilder validAccessTokenForURL(String parameterName)
    {
        this.securityCheck(new ValidAccessTokenBuilder(parameterName).path());
        return this;
    }
    
    public ExecBuilder requireSession()
    {
        this.securityCheck(new RequireSessionBuilder());
        return this;
    }
    
    public ExecBuilder wrapper(RouteWrapperBuilder builder)
    {
        this.wrapperBuilders.add(builder);
        return this;
    }

    public ExecBuilder verify()
    {
        if (this.handler == null) throw new IllegalStateException("Handler cannot be null");
        if (this.arguments == null) throw new IllegalStateException("Arguments array cannot be null");
        //
        for (int i = 0; i < this.arguments.length; i++)
        {
            ArgumentBuilder<?> ab = this.arguments[i];
            if (ab == null) throw new IllegalStateException("Missing argument " + i);
            ab.verify(this.handler.getParameterTypes()[i]);
        }
        return this;
    }
    
    public ExecutorClass writeClass()
    {
        ExecutorClass cls = new ExecutorClass("balsa.rt.executor." + this.router.getClass().getCanonicalName().toLowerCase(), this.handler.getName().substring(0, 1).toUpperCase() + this.handler.getName().substring(1) + this.getId() + "Executor", this.router.getClass().getCanonicalName(), this.router.getClass().getSimpleName());
        //
        StringBuilder sb = cls.getExecutorLogic();
        //
        sb.append("    // security pre-conditions\r\n");
        for (SecurityBuilder scb : this.securityBuilders)
        {
            scb.compile(cls);
        }
        // wrappers
        for (RouteWrapperBuilder wb : this.wrapperBuilders)
        {
            wb.compileBefore(cls);
        }
        //
        sb.append("    // bind the parameters\r\n");
        for (ArgumentBuilder<?> ab : this.arguments)
        {
            ab.compile(cls);
        }
        // wrappers
        for (RouteWrapperBuilder wb : this.wrapperBuilders)
        {
            wb.compileAfterBind(cls);
        }
        //
        sb.append("    // execute the handler\r\n");
        sb.append("    ");
        if (this.response != null)
        {
            cls.addImport(this.handler.getReturnType().getCanonicalName());
            sb.append(this.handler.getReturnType().getSimpleName()).append(" res = ");
        }
        sb.append("this.router.").append(this.handler.getName()).append("(");
        for (int i = 0; i < this.arity; i++)
        {
            if (i > 0) sb.append(", ");
            sb.append("p").append(i);
        }
        sb.append(");\r\n");
        //
        if (this.response != null)
        {
            this.response.compile(cls);
        }
        // wrappers
        for (RouteWrapperBuilder wb : this.wrapperBuilders)
        {
            wb.compileAfter(cls);
        }
        return cls;
    }

    @SuppressWarnings("unchecked")
    public Class<RouteExecutor<?>> compile() throws Exception
    {
        ExecutorClass cls = this.writeClass();
        // Compile the class
        return (Class<RouteExecutor<?>>) CompilerTool.getInstance().defineClass(cls.getCanonicalName(), cls.toString());
    }

    public RouteExecutor<?> executor() throws Exception
    {
        // compile the executor
        Class<? extends RouteExecutor<?>> cls = this.compile();
        // find the constructor
        Constructor<? extends RouteExecutor<?>> con = cls.getConstructor(new Class<?>[] { this.router.getClass() });
        // create the executor
        return con.newInstance(this.router);
    }

    public static ExecBuilder build(Route route) throws Exception
    {
        Logger logger = Logger.getLogger(ExecBuilder.class);
        // look at the structure and annotation of the given method and construct
        // the executor
        Router router = route.getRouter();
        Method method = route.getHandler();
        //
        ExecBuilder b = new ExecBuilder();
        b.router(router);
        b.handler(method);
        // security checks
        // check specified on the router
        for (Annotation secAnno : getSecurityAnnotation(router.getClass().getAnnotations()))
        {
            SecurityBuilder sb = getSecurityBuilder(secAnno);
            sb.fromAnnotation(secAnno);
            b.securityCheck(sb);
        }
        // checks specified on the method
        for (Annotation secAnno : getSecurityAnnotation(method.getAnnotations()))
        {
            SecurityBuilder sb = getSecurityBuilder(secAnno);
            sb.fromAnnotation(secAnno);
            b.securityCheck(sb);
        }
        // wrappers
        for (Annotation wrpAnno : getWrapperAnnotation(method.getAnnotations()))
        {
            RouteWrapperBuilder wb = getWrapperBuilder(wrpAnno);
            wb.fromAnnotation(wrpAnno);
            b.wrapper(wb);
        }
        // parameters
        Annotation[][] pa = method.getParameterAnnotations();
        Class<?>[] pt = method.getParameterTypes();
        int asIndex = 0;
        for (int i = 0; i < pt.length; i++)
        {
            Annotation[] annos = pa[i];
            Annotation argAnno = getArgumentAnnotation(annos);
            ArgumentBuilder<?> wArg = null;
            for (RouteWrapperBuilder wb : b.wrapperBuilders)
            {
                wArg = wb.argument(method, i, pt[i], annos);
                if (wArg != null) break;
            }
            // if we have a wrapper argument use it
            // else we have an annotation, use it
            // else use 'as'
            // else null
            if (wArg != null)
            {
                b.argument(wArg);
            }
            else if (argAnno != null)
            {
                ArgumentBuilder<?> ab = getArgumentBuilder(argAnno);
                ab.fromAnnotation(argAnno, annos, pt[i]);
                b.argument(ab);
            }
            else if (pt[i] == String.class && route.getCompiledPattern().as.length > asIndex )
            {
                b.parameterArgument(route.getCompiledPattern().as[asIndex]);
                asIndex++;
            }
            else
            {
                logger.warn("Binding null for argument " + i + " of route: " + route.toString());
                b.nullArgument();
            }
        }
        // response encoding
        Annotation resAnno = getResponseAnnotation(method.getAnnotations());
        if (resAnno != null)
        {
            ResponseBuilder rb = getResponseBuilder(resAnno);
            rb.fromAnnotation(resAnno, method.getAnnotations(), method.getReturnType());
            b.response(rb);
        }
        else if (void.class != method.getReturnType())
        {
            // the method is not void, its return value will be ignored
            logger.warn("The route handler: " + method.toString() + " returns a value yet specifies no response encoder, this return value will be ignored!");
        }
        // verify
        b.verify();
        return b;
    }

    private static Annotation getArgumentAnnotation(Annotation[] annos)
    {
        for (Annotation a : annos)
        {
            if (a.annotationType().getAnnotation(IsArgument.class) != null) return a;
        }
        return null;
    }

    private static ArgumentBuilder<?> getArgumentBuilder(Annotation a)
    {
        IsArgument arg = a.annotationType().getAnnotation(IsArgument.class);
        if (arg == null) return null;
        try
        {
            return arg.value().newInstance();
        }
        catch (Exception e)
        {
            // eat
        }
        return null;
    }

    private static Annotation getResponseAnnotation(Annotation[] annos)
    {
        for (Annotation a : annos)
        {
            if (a.annotationType().getAnnotation(IsResponse.class) != null) return a;
        }
        return null;
    }

    private static ResponseBuilder getResponseBuilder(Annotation a)
    {
        IsResponse res = a.annotationType().getAnnotation(IsResponse.class);
        if (res == null) return null;
        try
        {
            return res.value().newInstance();
        }
        catch (Exception e)
        {
            // eat
        }
        return null;
    }

    private static List<Annotation> getSecurityAnnotation(Annotation[] annos)
    {
        List<Annotation> ret = new LinkedList<Annotation>();
        for (Annotation a : annos)
        {
            if (a.annotationType().getAnnotation(IsSecurityCheck.class) != null) ret.add(a);
        }
        return ret;
    }

    private static SecurityBuilder getSecurityBuilder(Annotation a)
    {
        IsSecurityCheck res = a.annotationType().getAnnotation(IsSecurityCheck.class);
        if (res == null) return null;
        try
        {
            return res.value().newInstance();
        }
        catch (Exception e)
        {
            // eat
        }
        return null;
    }
    
    private static List<Annotation> getWrapperAnnotation(Annotation[] annos)
    {
        List<Annotation> ret = new LinkedList<Annotation>();
        for (Annotation a : annos)
        {
            if (a.annotationType().getAnnotation(IsRouteWrapper.class) != null) ret.add(a);
        }
        return ret;
    }

    private static RouteWrapperBuilder getWrapperBuilder(Annotation a)
    {
        IsRouteWrapper res = a.annotationType().getAnnotation(IsRouteWrapper.class);
        if (res == null) return null;
        try
        {
            return res.value().newInstance();
        }
        catch (Exception e)
        {
            // eat
        }
        return null;
    }

    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        RestrictedRouter rr = new RestrictedRouter();
        //
        for (Route r : Route.fromRouter("/", rr))
        {
            System.out.println("Route: " + r);
            System.out.println("Handler:\r\n" + ExecBuilder.build(r).writeClass());
            System.out.println(ExecBuilder.build(r).executor());
        }
        //        
        TestRouter tr = new TestRouter();
        for (Route r : Route.fromRouter("/", tr))
        {
            System.out.println("Route: " + r);
            System.out.println("Handler:\r\n" + ExecBuilder.build(r).writeClass());
            System.out.println(ExecBuilder.build(r).executor());
        }
    }

    public static class TestRouter extends Router
    {
        @Get("/test/:param")
        public void test(String param)
        {
        }

        @Get("/test1")
        public void test1(@Param("a") String a, @Header("b") String b, @Cookie("c") String c)
        {
        }

        @Any("/test2")
        @XML
        public XMLObj test2(@Param("a") String param)
        {
            return new XMLObj();
        }

        @Post("/test3")
        @XML
        public XMLObj test3(@Param("a") String param, @XML XMLObj in)
        {
            return in;
        }

        @XmlType(name = "test")
        @XmlRootElement(name = "test")
        public static class XMLObj
        {
            private String stat = "OK";

            @XmlElement(name = "stat")
            public String getStat()
            {
                return stat;
            }

            public void setStat(String stat)
            {
                this.stat = stat;
            }
        }
    }
    
    @Prefix("/")
    @RequireSession()
    @RequireValidPrincipal()
    public static class RestrictedRouter extends Router
    {        
        @Get("/restricted")
        @RequireValidPrincipal()
        public void restricted()
        {
            
        }
        
        @Get("/restricted/by/permission")
        @RequirePermissions("test.permission")
        public void restrictedByPermission()
        {
            
        }

        @Get("/restricted/by/permissions")
        @RequirePermissions({"test.permission", "another.permission"})
        public void restrictedByPermissions()
        {
            
        }
        
        @Get("/csrf/1")
        @RequireValidAccessToken()
        public void csrfCheck1()
        {
        }
        
        @Get("/csrf/2")
        @RequireValidAccessToken(@Param("token"))
        public void csrfCheck2()
        {
        }
        
        @Get("/csrf/3")
        @RequireValidAccessTokenForURL(value = @Param("token"))
        public void csrfCheck3()
        {
        }
        
        @Get("/csrf/4")
        @RequireValidAccessTokenForURL()
        @RequireSession()
        public void csrfCheck4()
        {
        }
    }
}
