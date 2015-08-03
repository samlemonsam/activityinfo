package org.activityinfo.i18n.tools.output;

import com.google.common.base.Function;

/**
 * Ensures that message translations escape single quotes as ''.
 */
public class MessageDecorator implements Function<String, String> {

    private static final char SINGLE_QUOTE = '\'';

    @Override
    public String apply(String input) {
        if(input.indexOf(SINGLE_QUOTE) == -1) {
            return input;
        }
        StringBuilder s = new StringBuilder();
        char lastChar = 0;
        for(int i=0;i!=input.length();++i) {
            char c = input.charAt(i);
            if(c == SINGLE_QUOTE) {
                // if there are two single quotes in a row, assume that they are already
                // escaped
                if(lastChar != SINGLE_QUOTE) {
                    s.append(SINGLE_QUOTE).append(SINGLE_QUOTE);
                }
            } else {
                s.append(c);
            }
            lastChar = c;
        }
        return s.toString();
    }
}
