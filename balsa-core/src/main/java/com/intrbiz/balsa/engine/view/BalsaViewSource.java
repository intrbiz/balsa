package com.intrbiz.balsa.engine.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;

public interface BalsaViewSource
{
    public static interface Formats
    {
        public static final String BALSA = "balsa";

        public static final String APT = "apt";

        public static final String MARKDOWN = "markdown";

        public static final String HTML = "html";
    }

    public static interface Charsets
    {
        public static final Charset UTF8 = Charset.forName("UTF8");
    }

    public static abstract class Resource
    {

        private final String name;

        private final String format;

        private final Charset charset;

        public Resource(String name, String format, Charset charset)
        {
            super();
            if (name == null) throw new IllegalArgumentException("Name cannot be null!");
            if (format == null) throw new IllegalArgumentException("Format cannot be null!");
            this.name = name;
            this.format = format;
            this.charset = charset;
        }

        /**
         * The name of the resource
         * @return
         */
        public final String getName()
        {
            return this.name;
        }

        /**
         * The format of the resource, eg: balsa, apt, markdown
         * @return
         */
        public final String getFormat()
        {
            return this.format;
        }

        /**
         * The charset of the resource
         * 
         * @return
         */
        public final Charset getCharset()
        {
            return this.charset;
        }

        /**
         * 
         * @return
         * @throws IOException
         */
        public abstract InputStream openStream() throws IOException;

        /**
         * Open the resource as a character stream
         * 
         * @return
         * @throws IOException
         */
        public Reader openReader() throws IOException
        {
            return new BufferedReader(new InputStreamReader(this.openStream(), this.charset != null ? this.charset : Charset.defaultCharset()));
        }
    }

    /**
     * Locate the view source data ready for parsing
     * 
     * @param name
     *            the view name
     * @param context
     *            the current context
     * @return the resource or null if it does not exist
     * @throws BalsaException
     */
    Resource open(String name, BalsaContext context) throws BalsaException;
}
