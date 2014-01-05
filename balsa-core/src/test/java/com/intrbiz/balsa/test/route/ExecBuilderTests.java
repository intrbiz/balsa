package com.intrbiz.balsa.test.route;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.impl.route.exec.ExecBuilder;
import com.intrbiz.balsa.test.route.model.RestrictedRouter;
import com.intrbiz.balsa.test.route.model.TestRouter;

public class ExecBuilderTests
{
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
}
