package com.intrbiz.balsa.engine.route.impl.exec.compiler;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class MemoryJavaFile extends SimpleJavaFileObject
{
    final String code;

    public MemoryJavaFile(String name, String code)
    {
        super(URI.create("mem:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return code;
    }
}
