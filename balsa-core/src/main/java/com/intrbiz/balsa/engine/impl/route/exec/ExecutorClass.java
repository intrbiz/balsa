package com.intrbiz.balsa.engine.impl.route.exec;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaValidationError;

public class ExecutorClass
{
    public final String packageName;
    
    private Set<String> imports = new TreeSet<String>();
    
    public final String name;
    
    public final String routerClass;
    
    public final String routerType;
    
    private int fieldSeq = 0;
    
    private Map<String, String> fields = new LinkedHashMap<String, String>();
    
    private StringBuilder constructorLogic = new StringBuilder();
    
    private StringBuilder executorLogic = new StringBuilder();
    
    private int executorVariableSeq = 0;
    
    private Map<String, String> executorVariables = new HashMap<String, String>();
    
    public ExecutorClass(String pack, String name, String routerClass, String routerType)
    {
        super();
        this.packageName = pack;
        this.name = name;
        this.routerClass = routerClass;
        this.routerType = routerType;
        //
        this.imports.add(routerClass);
        this.imports.add("com.intrbiz.balsa.engine.route.RouteExecutor");
        this.imports.add("com.intrbiz.balsa.BalsaContext");
        this.imports.add(Method.class.getCanonicalName());
        this.imports.add(BalsaConversionError.class.getCanonicalName());
        this.imports.add(BalsaValidationError.class.getCanonicalName());
    }
    
    public void addField(String type, String name)
    {
        this.fields.put(name, type);
    }
    
    public void addImport(String type)
    {
        this.imports.add(type);
    }
    
    public StringBuilder getConstructorLogic()
    {
        return this.constructorLogic;
    }
    
    public StringBuilder getExecutorLogic()
    {
        return this.executorLogic;
    }
    
    public String getCanonicalName()
    {
        return this.packageName + "." + this.name;
    }
    
    public String allocateField(String type, String namePrefix)
    {
        String name = namePrefix + "_" + (this.fieldSeq++);
        this.fields.put(name, type);
        return name;
    }
    
    public String allocateExecutorVariable(String type)
    {
        return this.allocateExecutorVariable(type, "v");
    }
    
    public String allocateExecutorVariable(String type, String prefix)
    {
        String var = prefix + "_" + (this.executorVariableSeq++);
        this.executorVariables.put(var, type);
        return var;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        //
        sb.append("package ").append(this.packageName).append(";\r\n");
        sb.append("\r\n");
        for (String imp : this.imports)
        {
            sb.append("import ").append(imp).append(";\r\n");    
        }
        //
        sb.append("\r\n");
        sb.append("public class ").append(this.name).append(" extends RouteExecutor<").append(this.routerType).append(">\r\n");
        sb.append("{\r\n");
        if (! this.fields.isEmpty()) sb.append("\r\n");
        //
        for (Entry<String, String> e : this.fields.entrySet())
        {
            sb.append("  private ").append(e.getValue()).append(" ").append(e.getKey()).append(";\r\n");
        }
        sb.append("\r\n");
        //
        sb.append("  public ").append(this.name).append("(").append(this.routerType).append(" router, Method handler) throws Exception\r\n");
        sb.append("  {\r\n");
        sb.append("    super(router, handler);\r\n");
        sb.append(this.constructorLogic.toString());
        sb.append("  }\r\n");
        sb.append("\r\n");
        //
        sb.append("  public void execute(BalsaContext context) throws Throwable\r\n");
        sb.append("  {\r\n");
        // debug out all allocated variables
        for (Entry<String, String> e : this.executorVariables.entrySet())
        {
            sb.append("    //").append(e.getValue()).append(" ").append(e.getKey()).append(";\r\n");
        }
        //
        sb.append(this.executorLogic.toString());
        sb.append("  }\r\n");
        sb.append("\r\n");        
        //
        sb.append("}\r\n");
        sb.append("\r\n");
        //
        return sb.toString();
    }
}
