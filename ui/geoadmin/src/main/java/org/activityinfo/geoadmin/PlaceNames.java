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
package org.activityinfo.geoadmin;

import org.activityinfo.io.match.names.LatinPlaceNameScorer;

/**
 * Utilities for matching place names.
 */
public class PlaceNames {



    /**
     * Cleans up a name by removing all punctuation and non-letters.
     */
    public static String cleanName(String name) {
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i != name.length(); ++i) {
            int cp = name.codePointAt(i);
            if (Character.isLetter(cp)) {
                cleaned.appendCodePoint(Character.toLowerCase(cp));
            }
        }
        return cleaned.toString();
    }
    
    /**
     * gets the similarity of the two strings using Jaro distance.
     *
     * @param string1 the first input string
     * @param string2 the second input string
     * @return a value between 0-1 of the similarity
     */
    public static double similarity(final String string1, final String string2) {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
        return scorer.score(string1, string2);
    }

}
