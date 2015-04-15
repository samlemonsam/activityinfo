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
        //System.out.println(x + " <> " + y);
        return distanceMatrix[key(x,y)];
    }
}
