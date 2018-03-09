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
package org.activityinfo.io.match.names;

/**
 *
 */
public class ConsonantSimilarity {

    private static ConsonantSimilarity INSTANCE;

    private static final int NUM_CONSONANTS = 26;
    public static final char FIRST_CONSONANT = 'A';

    private double distanceMatrix[];


    public static ConsonantSimilarity get() {
        if(INSTANCE == null) {
            INSTANCE = new ConsonantSimilarity();
        }
        return INSTANCE;
    }

    private ConsonantSimilarity() {
        distanceMatrix = new double[NUM_CONSONANTS*NUM_CONSONANTS];

        for(int i=0;i!=distanceMatrix.length;++i) {
            distanceMatrix[i] = Double.POSITIVE_INFINITY;
        }

        define('M', 'N', 0.5);
        define('K', 'Q', 0.25);
        define('C', 'S', 0.5);
    }


    private void define(char a, char b, double distance) {
        distanceMatrix[key(a,b)] = distance;
    }

    private int key(char a, char b) {
        if(a < b) {
            return (a - FIRST_CONSONANT) * NUM_CONSONANTS + (b - 'A');
        } else {
            return (b - FIRST_CONSONANT) * NUM_CONSONANTS + (a - 'A');
        }
    }


    public double distance(char x, char y) {
        assert ('A' <= x && x <= 'Z') : "x is not a letter";
        assert ('A' <= y && y <= 'Z') : "y is not a letter";

        //System.out.println(x + " <> " + y);
        return distanceMatrix[key(x,y)];
    }
}
