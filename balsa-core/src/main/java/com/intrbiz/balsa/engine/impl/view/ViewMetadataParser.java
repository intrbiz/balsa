package com.intrbiz.balsa.engine.impl.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern META_BLOCK = Pattern.compile("(?m)^---$((?s).+)^---$");

    private static final Pattern META_DATA = Pattern.compile("(?m)^([A-Za-z0-9_]+):\\s*(.*)$");

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
