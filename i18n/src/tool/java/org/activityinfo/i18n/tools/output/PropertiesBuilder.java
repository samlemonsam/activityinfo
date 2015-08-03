package org.activityinfo.i18n.tools.output;


import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.i18n.tools.model.ResourceClass;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Convenience class for writing Java property files. We use this 
 * custom class rather than {@link java.util.Properties#store(java.io.OutputStream, String)} 
 * so that we can control the order of property keys to simplify version control merges.
 */
public class PropertiesBuilder {


    private final StringBuilder writer = new StringBuilder();
    private Function<String, String> translationDecorator = Functions.identity();
    
    private int missingCount = 0;


    public void addAll(ResourceClass resourceClass, TranslationSet translations) {

        InspectingVisitor visitor = resourceClass.inspect();

        if(visitor.isMessageSubtype()) {
            this.translationDecorator = new MessageDecorator();
        }

        addAll(visitor.getKeys(), translations);
    }


    public void addAll(Collection<String> keySet, TranslationSet translations) {
        List<String> keys = Lists.newArrayList(keySet);
        Collections.sort(keys);
        
        for(String key : keys) {
            String translated = translations.get(key);
            if(!Strings.isNullOrEmpty(translated)) {
                add(key, translated);
            } else {
                missingCount++;
            }
        }
    }

    public int getMissingCount() {
        return missingCount;
    }

    public void add(String key, String value) {
        writer.append(escapeKey(key));
        writer.append("=");
        writer.append(escapeValue(translationDecorator.apply(value)));
        writer.append('\n');
    }
    
    public void addComment(String comment) throws IOException {
        writer.append("# ");
        writer.append(comment);
        writer.append("\n");
    }
    
    public String toString() {
        return writer.toString();
        
    }

    public static String escapeKey(String key) {
        return escape(key, true, false);
    }

    public static String escapeValue(String value) {
        return escape(value, false, false);
    }

    /*
    * Converts unicodes to encoded &#92;uxxxx and escapes
    * special characters with a preceding slash
    */
    public static String escape(String theString,
                               boolean escapeSpace,
                               boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                    break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                    break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                    break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }


    /**
     * Convert a nibble to a hex character
     * @param   nibble  the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = {
            '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    public void setTranslationDecorator(MessageDecorator messageDecorator) {
        this.translationDecorator = messageDecorator;
    }
}
