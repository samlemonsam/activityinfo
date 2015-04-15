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
