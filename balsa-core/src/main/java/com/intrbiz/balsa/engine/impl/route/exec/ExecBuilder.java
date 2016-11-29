package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ArgumentBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ConverterBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.argument.CookieArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.CurrentPrincipalArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.HeaderArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.JSONArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ListParameterArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.NullArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ParameterArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ValidatorBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.argument.XMLArgument;
import com.intrbiz.balsa.engine.impl.route.exec.response.JSONResponse;
import com.intrbiz.balsa.engine.impl.route.exec.response.ResponseBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.response.XMLResponse;
import com.intrbiz.balsa.engine.impl.route.exec.security.PermissionsBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.security.RequireSessionBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.security.SecurityBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.security.ValidAccessTokenBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.security.ValidPrincipalBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.wrapper.RouteWrapperBuilder;
import com.intrbiz.balsa.engine.route.RouteExecutor;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.converter.Converter;
import com.intrbiz.metadata.IsArgument;
import com.intrbiz.metadata.IsResponse;
import com.intrbiz.metadata.IsRouteWrapper;
import com.intrbiz.metadata.IsSecurityCheck;
import com.intrbiz.util.compiler.CompilerTool;
import com.intrbiz.validator.Validator;

public class ExecBuilder
{
    private Logger logger = Logger.getLogger(ExecBuilder.class);
    
    private int arity = 0;

    private int currentArgumentIndex = 0;

    private ArgumentBuilder<?>[] arguments;
    
    private ConverterBuilder[] converters;
    
    private ValidatorBuilder[] validators;

    private Method handler;
    
    private boolean exceptionHandler;

    private Router<?> router;

    private ResponseBuilder response = null;
    
    private List<SecurityBuilder> securityBuilders = new LinkedList<SecurityBuilder>();
    
    private List<RouteWrapperBuilder> wrapperBuilders = new LinkedList<RouteWrapperBuilder>();

    public ExecBuilder()
    {
        super();
    }

    public int getArity()
    {
        return arity;
    }

    public ArgumentBuilder<?>[] getArguments()
    {
        return arguments;
    }

    public ConverterBuilder[] getConverters()
    {
        return converters;
    }

    public ValidatorBuilder[] getValidators()
    {
        return validators;
    }

    public Method getHandler()
    {
        return handler;
    }

    public boolean isExceptionHandler()
    {
        return exceptionHandler;
    }

    public Router<?> getRouter()
    {
        return router;
    }

    public ResponseBuilder getResponse()
    {
        return response;
    }

    public List<SecurityBuilder> getSecurityBuilders()
    {
        return securityBuilders;
    }

    public List<RouteWrapperBuilder> getWrapperBuilders()
    {
        return wrapperBuilders;
    }

    public ExecBuilder handler(Method handler, boolean exceptionHandler)
    {
        this.handler = handler;
        this.exceptionHandler = exceptionHandler;
        this.arity = handler.getParameterTypes().length;
        this.arguments = new ArgumentBuilder[this.arity];
        this.converters = new ConverterBuilder[this.arity];
        this.validators = new ValidatorBuilder[this.arity];
        return this;
    }

    public ExecBuilder router(Router<?> router)
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
    
    public ExecBuilder listParameterArgument(String name)
    {
        return argument(new ListParameterArgument().name(name));
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
        return jsonArgument(this.handler.getParameterTypes()[this.currentArgumentIndex]);
    }
    
    public ExecBuilder currentPrincipalArgument(Class<?> type)
    {
        return argument(new CurrentPrincipalArgument().type(type));
    }
    
    public ExecBuilder currentPrincipalArgument()
    {
        return this.currentPrincipalArgument(this.handler.getParameterTypes()[this.currentArgumentIndex]);
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
    
    public ExecBuilder withConverter(Converter<?> converter)
    {
        // add
        if (this.currentArgumentIndex <= 0) throw new IllegalStateException("No arguments are defined, cannot add a converter");
        if (this.arguments[this.currentArgumentIndex -1] == null) throw new IllegalArgumentException("Cannot attach the converter to a null argument");
        this.converters[this.currentArgumentIndex -1] = new ConverterBuilder(this.currentArgumentIndex -1, converter, List.class.isAssignableFrom(this.handler.getParameterTypes()[this.currentArgumentIndex -1]));
        return this;
    }
    
    public ExecBuilder withValidator(Validator<?> validator)
    {
        // add
        if (this.currentArgumentIndex <= 0) throw new IllegalStateException("No arguments are defined, cannot add a validator");
        if (this.arguments[this.currentArgumentIndex -1] == null) throw new IllegalArgumentException("Cannot attach the validator to a null argument");
        this.validators[this.currentArgumentIndex -1] = new ValidatorBuilder(this.currentArgumentIndex -1, validator, List.class.isAssignableFrom(this.handler.getParameterTypes()[this.currentArgumentIndex -1]));
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
            ConverterBuilder   cb = this.converters[i];
            if (ab == null) throw new IllegalStateException("Missing argument " + i);
            if (cb == null)
            {
                ab.verify(this.handler.getParameterTypes()[i]);
            }
            else
            {
                if (List.class.isAssignableFrom(this.handler.getParameterTypes()[i]))
                {
                    logger.debug("Skiping verification of parameter " + i + " as it is a List type");
                }
                else
                {
                    ab.verify(cb.getFromType());
                    cb.verify(this.handler.getParameterTypes()[i]);
                }
            }
        }
        return this;
    }
    
    private String getExecutorSignature()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getArity());
        for (Class<?> parameterType : this.handler.getParameterTypes())
        {
            sb.append(parameterType.getSimpleName().replaceAll("$", ""));
        }
        return sb.toString();
    }
    
    private Class<?> loadPrecompiledExecutor()
    {
        String executorClassName = "balsa.rt.executor." + this.router.getClass().getCanonicalName().toLowerCase() + "." + this.handler.getName().substring(0, 1).toUpperCase() + this.handler.getName().substring(1) + this.getExecutorSignature() + "Executor";
        try
        {
            Class<?> cls = Class.forName(executorClassName);
            logger.info("Using precompiled executor: " + executorClassName);
            return cls;
        }
        catch (ClassNotFoundException e)
        {
            logger.info("Could not load precompiled executor: " + executorClassName);
        }
        return null;
    }
    
    public ExecutorClass writeClass()
    {
        String executorPackageName = "balsa.rt.executor." + this.router.getClass().getCanonicalName().toLowerCase();
        String executorSimpleName  = this.handler.getName().substring(0, 1).toUpperCase() + this.handler.getName().substring(1) + this.getExecutorSignature() + "Executor";
        ExecutorClass cls = new ExecutorClass(executorPackageName, executorSimpleName, this.router.getClass().getCanonicalName(), this.router.getClass().getSimpleName());
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
        // bind parameters
        String[] parameterVariables = new String[this.arity];
        sb.append("    // bind the parameters\r\n");
        for (int i = 0; i < this.arguments.length; i++)
        {
            ArgumentBuilder<?> ab = this.arguments[i];
            ab.compile(cls);
            parameterVariables[i] = ab.getVariable();
        }
        // apply any converters
        sb.append("    // convert any parameters\r\n");
        for (int i = 0; i < this.converters.length; i++)
        {
            ConverterBuilder conv = this.converters[i];
            if (conv != null)
            {
                conv.compile(cls, parameterVariables[i]);
                parameterVariables[i] = conv.getVariable();
            }
        }
        if (! this.exceptionHandler)
        {
            sb.append("    // throw a conversion exception?\r\n");
            sb.append("    if (context.hasConversionErrors()) throw new BalsaConversionError();\r\n");
        }
        // apply any validators
        sb.append("    // validate any parameters\r\n");
        for (int i = 0; i < this.validators.length; i++)
        {
            ValidatorBuilder vald = this.validators[i];
            if (vald != null)
            {
                vald.compile(cls, parameterVariables[i]);   
            }
        }
        if (! this.exceptionHandler)
        {
            sb.append("    // throw a validation exception?\r\n");
            sb.append("    if (context.hasValidationErrors()) throw new BalsaValidationError();\r\n");
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
        for (int i = 0; i < this.arguments.length; i++)
        {
            if (i > 0) sb.append(", ");
            sb.append(parameterVariables[i]);
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
        // check for a precompiled class
        Class<?> compiled = this.loadPrecompiledExecutor();
        if (compiled == null)
        {
            // generate the class
            ExecutorClass cls = this.writeClass();
            // Compile the class
            compiled = CompilerTool.getInstance().defineClass(cls.getCanonicalName(), cls.toString());
        }
        return (Class<RouteExecutor<?>>) compiled;
    }

    public RouteExecutor<?> executor() throws Exception
    {
        // compile the executor
        Class<? extends RouteExecutor<?>> cls = this.compile();
        // find the constructor
        Constructor<? extends RouteExecutor<?>> con = cls.getConstructor(new Class<?>[] { this.router.getClass(), Method.class });
        // create the executor
        return con.newInstance(this.router, this.handler);
    }

    public static ExecBuilder build(Route route) throws Exception
    {
        Logger logger = Logger.getLogger(ExecBuilder.class);
        // look at the structure and annotation of the given method and construct
        // the executor
        Router<?> router = route.getRouter();
        Method method = route.getHandler();
        //
        ExecBuilder b = new ExecBuilder();
        b.router(router);
        b.handler(method, route.isExceptionHandler());
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
            //
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
            else if (route.getCompiledPattern().as.length > asIndex)
            {
                b.parameterArgument(route.getCompiledPattern().as[asIndex]);
                asIndex++;
            }
            else
            {
                logger.warn("Binding null for argument " + i + " of route: " + route.toString());
                b.nullArgument();
            }
            // converter
            Converter<?> converter = Converter.fromParameter(pt[i], annos);
            if (converter != null)
            {
                logger.trace("Paramater with converter: " + converter);
                b.withConverter(converter);
            }
            // validator
            Validator<?> validator = Validator.fromParameter(pt[i], annos);
            if (validator != null)
            {
                logger.trace("Paramater with validator: " + validator);
                b.withValidator(validator);
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
}
