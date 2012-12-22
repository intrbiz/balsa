package com.intrbiz.balsa.util;

import static com.intrbiz.Util.htmlEncode;

import java.io.IOException;
import java.io.Writer;

public class HTMLWriter extends Writer
{
    private Writer writer;
    
    private int indentLevel = 0;
    
    public HTMLWriter(Writer writer)
    {
        super();
        this.writer = writer;
    }

    public void write(int c) throws IOException
    {
        writer.write(c);
    }

    public void write(char[] cbuf) throws IOException
    {
        writer.write(cbuf);
    }

    public void write(char[] cbuf, int off, int len) throws IOException
    {
        writer.write(cbuf, off, len);
    }

    public void write(String str) throws IOException
    {
        writer.write(str);
    }
    
    public void writeEncoded(String str) throws IOException
    {
        htmlEncode(str, this);
    }

    public void write(String str, int off, int len) throws IOException
    {
        writer.write(str, off, len);
    }

    public Writer append(CharSequence csq) throws IOException
    {
        return writer.append(csq);
    }

    public Writer append(CharSequence csq, int start, int end) throws IOException
    {
        return writer.append(csq, start, end);
    }

    public Writer append(char c) throws IOException
    {
        return writer.append(c);
    }

    public void flush() throws IOException
    {
        writer.flush();
    }

    public void close() throws IOException
    {
        writer.close();
    }
    
    public HTMLWriter indent()
    {
        this.indentLevel++;
        return this;
    }
    
    public HTMLWriter unindent()
    {
        this.indentLevel--;
        if (this.indentLevel < 0) this.indentLevel = 0;
        return this;
    }
    
    public int getIndentLevel()
    {
        return this.indentLevel;
    }
    
    public HTMLWriter padding() throws IOException
    {
        for (int i = 0; i < this.getIndentLevel(); i++)
        {
            this.write("\t");
        }
        return this;
    }
    
    public HTMLWriter put(String str) throws IOException
    {
        this.write(str);
        return this;
    }
    
    public HTMLWriter putLn(String str) throws IOException
    {
        this.write(str);
        this.write("\r\n");
        return this;
    }
    
    public HTMLWriter putPad(String str) throws IOException
    {
        this.padding();
        this.write(str);
        return this;
    }
    
    public HTMLWriter putPadLn(String str) throws IOException
    {
        this.padding();
        this.write(str);
        this.write("\r\n");
        return this;
    }
    
    public HTMLWriter putEnc(String str) throws IOException
    {
        this.writeEncoded(str);
        return this;
    }
    
    public HTMLWriter putEncLn(String str) throws IOException
    {
        this.writeEncoded(str);
        this.write("\r\n");
        return this;
    }
    
    public HTMLWriter putEncPadLn(String str) throws IOException
    {
        this.padding();
        this.writeEncoded(str);
        this.write("\r\n");
        return this;
    }
    
    public HTMLWriter ln() throws IOException
    {
        this.write("\r\n");
        return this;
    }
    
    public HTMLWriter endTag(String name) throws IOException
    {
        this.write("</");
        this.write(name);
        this.write(">");
        return this;
    }
    
    public HTMLWriter startTag(String name) throws IOException
    {
        this.write("<");
        this.write(name);
        this.write(">");
        return this;
    }
    
    public HTMLWriter openStartTag(String name) throws IOException
    {
        this.write("<");
        this.write(name);
        return this;
    }
    
    public HTMLWriter attribute(String name, String value) throws IOException
    {
        this.write(" ");
        this.write(name);
        this.write("=\"");
        this.writeEncoded(value);
        this.write("\"");
        return this;
    }
    
    public HTMLWriter closeStartTag() throws IOException
    {
        this.write(">");
        return this;
    }
    
    public HTMLWriter endTagPad(String name) throws IOException
    {
        this.unindent();
        this.padding();
        this.write("</");
        this.write(name);
        this.write(">");
        return this;
    }
    
    public HTMLWriter startTagPad(String name) throws IOException
    {
        this.padding();
        this.indent();
        this.write("<");
        this.write(name);
        this.write(">");
        return this;
    }
    
    public HTMLWriter openStartTagPad(String name) throws IOException
    {
        this.padding();
        this.indent();
        this.write("<");
        this.write(name);
        return this;
    }
    
    public HTMLWriter endTagPadLn(String name) throws IOException
    {
        this.unindent();
        this.padding();
        this.write("</");
        this.write(name);
        this.write(">\r\n");
        return this;
    }
    
    public HTMLWriter startTagPadLn(String name) throws IOException
    {
        this.padding();
        this.indent();
        this.write("<");
        this.write(name);
        this.write(">\r\n");
        return this;
    }
    
    public HTMLWriter endTagLn(String name) throws IOException
    {
        this.write("</");
        this.write(name);
        this.write(">\r\n");
        return this;
    }
    
    public HTMLWriter startTagLn(String name) throws IOException
    {
        this.write("<");
        this.write(name);
        this.write(">\r\n");
        return this;
    }
    
    public HTMLWriter closeStartTagLn() throws IOException
    {
        this.write(">\r\n");
        return this;
    }
    
    public HTMLWriter comment(String comment) throws IOException
    {
        this.write("<!-- ");
        this.write(comment);
        this.write(" -->");
        return this;
    }
}
