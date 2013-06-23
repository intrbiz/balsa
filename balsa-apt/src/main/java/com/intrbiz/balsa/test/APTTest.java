package com.intrbiz.balsa.test;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.apache.maven.doxia.module.apt.AptParser;

import com.intrbiz.balsa.doxia.BalsaSink;
import com.intrbiz.balsa.view.component.Component;

public class APTTest
{
    public static void main(String[] args) throws Exception
    {
        try (Reader r = new FileReader(new File("test.apt")))
        {
            // the output
            BalsaSink sink = new BalsaSink(null);
            new AptParser().parse(r, sink);
            //
            Component root = sink.getRoot();
            System.out.println(root.toXML(""));
        }
    }
}
