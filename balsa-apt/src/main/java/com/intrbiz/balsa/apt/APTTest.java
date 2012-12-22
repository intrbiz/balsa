package com.intrbiz.balsa.apt;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.apache.maven.doxia.module.apt.AptParser;

import com.intrbiz.balsa.view.component.Component;

public class APTTest
{
    public static void main(String[] args) throws Exception
    {
        try (Reader r = new FileReader(new File("test2.apt")))
        {
            // the output
            BalsaSink2 sink = new BalsaSink2();
            //
            new AptParser().parse(r, sink);
            //
            Component root = sink.getRoot();
            System.out.println(root.toXML(""));
        }
    }
}
