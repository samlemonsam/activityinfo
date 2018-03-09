/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
