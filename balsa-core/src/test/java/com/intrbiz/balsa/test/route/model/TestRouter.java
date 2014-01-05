package com.intrbiz.balsa.test.route.model;

import java.util.Date;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsDate;
import com.intrbiz.metadata.AsInt;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Cookie;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Header;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.XML;

@Prefix("/")
public class TestRouter extends Router
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
    
    @Get("/test4/:param")
    public void test4(@Param("param") String param)
    {
    }
    
    @Get("/test/asuuid/:param")
    public void testAsUUID(@Param("param") @AsUUID UUID param)
    {
    }
    
    @Get("/test/asint/:param")
    public void testAsInt(@AsInt int param)
    {
    }
    
    @Get("/test/asint/:param")
    public void testAsDate(@AsDate Date param)
    {
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
