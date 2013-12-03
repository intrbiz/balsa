package com.intrbiz;

import static com.intrbiz.Util.isEmpty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.intrbiz.balsa.BalsaApplication;

/**
 * Bootstrap a Balsa application from the command line
 */
public class Balsa
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        try
        {
            Map<String, String> arguments = parseArguments(args);
            // help?
            if (arguments.containsKey("help"))
            {
                usage();
                System.exit(0);
            }
            // Setup logging
            configureLogging(arguments);
            // Load the app
            String appClassName = arguments.get("app");
            if (!isEmpty(appClassName))
            {
                System.err.println("Starting Balsa Application: " + appClassName);
                Class<? extends BalsaApplication> appClass = (Class<? extends BalsaApplication>) Class.forName(appClassName);
                // Load the app
                BalsaApplication app = appClass.newInstance();
                // Set the args
                app.arguments(arguments);
                // Start the app
                app.start();
                // Enter some form of run loop
            }
            else
            {
                error("The application class was not provided, cannot start!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            error("Error starting the application:\n\t" + e.getMessage());
        }
    }

    private static void configureLogging(Map<String, String> arguments)
    {
        String logging = arguments.get("logging");
        if (isEmpty(logging))
        {
            Level level = Level.ERROR;
            if (arguments.containsKey("trace"))
                level = Level.TRACE;
            else if (arguments.containsKey("debug"))
                level = Level.DEBUG;
            else if (arguments.containsKey("info"))
                level = Level.INFO;
            else if (arguments.containsKey("warn"))
                level = Level.WARN;
            else if (arguments.containsKey("error"))
                level = Level.ERROR;
            else if (arguments.containsKey("fatal")) level = Level.FATAL;
            // Configure for console logging
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(level);
        }
        else
        {
            // Use the properties file
            File props = new File(logging);
            if (props.exists())
            {
                System.err.println("Using logging config file: " + props);
                PropertyConfigurator.configure(props.getAbsolutePath());
            }
            else
            {
                error("The specified logging properties file '" + logging + "' does not exist!");
            }
        }
    }

    private static void usage()
    {
        System.err.println("Balsa - Start a Balsa application");
        System.err.println("\tBalsa --app <app classname>");
        System.err.println("");
        System.err.println("");
        System.err.println("Usage:");
        System.err.println("\tBalsa accepts a number of arguments, in the form:");
        System.err.println("\t\t'--name' 'value'");
        System.err.println("");
        System.err.println("\tRequired arguments:");
        System.err.println("\t\t'--app'              'class.name'  The Balsa application classname to start");
        System.err.println("");
        System.err.println("\tCommon arguments:");
        System.err.println("\t\t'--dev'                            Development mode (disables view caching)");
        System.err.println("\t\t'--port'             '8090'        The port to listen on");
        System.err.println("\t\t'--workers'          '16'          The number of worker threads to use");
        System.err.println("\t\t'--views'            'views'       The view base path");
        System.err.println("\t\t'--session-lifetime' '30'          The session lifetime in minutes");
        System.err.println("\t\t'--logging'          'console'     The Log4J properties file");
        System.err.println("\t\t'--trace'                          The Log4J trace level");
        System.err.println("\t\t'--debug'                          The Log4J debug level");
        System.err.println("\t\t'--info'                           The Log4J info  level");
        System.err.println("\t\t'--warn'                           The Log4J warn  level");
        System.err.println("\t\t'--error'                          The Log4J error level");
        System.err.println("\t\t'--fatal'                          The Log4J fatal level");
        System.err.println("");
        System.err.println("");
        System.err.println("Example:");
        System.err.println("\tBalsa --app 'example.App' --port 8090 --workers 16");
    }

    private static Map<String, String> parseArguments(String[] args)
    {
        Map<String, String> m = new HashMap<String, String>();
        String name = null;
        for (String arg : args)
        {
            if (arg.startsWith("--"))
            {
                if (name == null)
                {
                    name = arg.substring(2);
                }
                else
                {
                    // flag arg
                    m.put(name, "true");
                    name = arg.substring(2);
                }
            }
            else
            {
                if (name != null)
                {
                    m.put(name, arg);
                    name = null;
                }
                else
                {
                    // error
                    error("Unable to parse application arguments, found the value: '" + arg + "' without a name!");
                }
            }
        }
        // possible trailing flag
        if (name != null) m.put(name, "true");
        return m;
    }

    private static void error(String message)
    {
        System.err.println(message);
        System.err.println("");
        usage();
        System.exit(1);
    }
}
