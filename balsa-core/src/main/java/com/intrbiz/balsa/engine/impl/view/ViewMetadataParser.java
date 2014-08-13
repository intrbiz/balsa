package com.intrbiz.balsa.engine.impl.view;

import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.view.ViewMetadata;

/**
 * Extract metadata from plain text based views like Markdown and APT
 * 
 * The metadata must be at the start of the file, in the format:
 * 
 * ---
 * name: value
 * name: value
 * ---
 * 
 * 
 */
public class ViewMetadataParser
{
    private static Logger logger = Logger.getLogger(ViewMetadataParser.class);

    private static final Pattern META_BLOCK = Pattern.compile("(?m)^---$((?s).+)^---$");

    private static final Pattern META_DATA = Pattern.compile("(?m)^([A-Za-z0-9_]+):\\s*(.*)$");
    
    public static ViewMetadata extractMetadata(File file)
    {
        try (FileReader in = new FileReader(file))
        {
            char[] buffer = new char[1024];
            int length = in.read(buffer);
            if (length > 0)
            {
                return extractMetadata(new String(buffer, 0, length));
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to parse view metadata", e);
        }
        return null;
    }
    
    public static ViewMetadata extractMetadata(String text)
    {
        ViewMetadata md = new ViewMetadata();
        extractMetadata(text, md);
        return md;
    }

    public static String extractMetadata(String text, ViewMetadata metadata)
    {
        Matcher block = META_BLOCK.matcher(text);
        if (block.find())
        {
            String meta = block.group(1);
            parseMetadata(meta, metadata);
            return text.substring(block.end());
        }
        return text;
    }

    private static void parseMetadata(String meta, ViewMetadata metadata)
    {
        Matcher data = META_DATA.matcher(meta);
        while (data.find())
        {
            metadata.setAttribute(data.group(1), data.group(2));
        }
    }
}
