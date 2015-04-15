package org.activityinfo.io.match.names;

import static java.lang.Math.min;

/**
 * Calculates the distance between two words written in latin script.
 * 
 */
public class LatinWordDistance {


    static final double EXTRA_VOWEL_COST = 0.30;

    static final double TRAILING_VOWELS_COST = 0.5;

    static final double VOWEL_SUBSTITUTION_COST = 0.20;

    static final double DOUBLED_CONSONANT_COST = 0.5;

    static final double CLOSING_CONSONANT_COST = 0.75;

    static final char NOTHING = 32;


    public static class Word {

        private char[] chars;

        private int start;

        /**
         * The index of the end of the underlying character array (exclusive)
         */
        private int end;


        public void set(String s) {
            this.chars = s.toCharArray();
            this.start = 0;
            this.end = s.length();
        }

        public void set(char[] charArray, int start, int end) {
            this.chars = charArray;
            this.start = start;
            this.end = end;
        }

        public boolean isEndOfWord(int i) {
            return i >= end;
        }

        public char at(int i) {
            if(i < end) {
                return chars[i];
            } else {
                return NOTHING;
            }
        }

        public boolean isRepeated(int i) {
            return i > start && chars[i] == chars[i-1];
        }

        public boolean isVowel(int i) {
            return i >= start && i < end && isVowelChar(chars[i]);
        }

        public boolean isLast(int i) {
            return i+1 == end;
        }

        public int length() {
            return end - start;
        }
    }

    private final ConsonantSimilarity consonants = ConsonantSimilarity.get();
    private final Word x = new Word();
    private final Word y = new Word();

    public static boolean isVowelChar(char c) {
        return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U';
    }

    public double similarity(String xs, String ys) {
        this.x.set(xs);
        this.y.set(ys);

        return similarity();
    }


    public double similarity(char[] x, int x0, int x1, char[] y, int y0, int y1) {
        this.x.set(x, x0, x1);
        this.y.set(y, y0, y1);

        return similarity();
    }

    private double similarity() {
        double distance = distance(this.x.start, this.y.start);
        if(Double.isInfinite(distance)) {
            return 0;
        }

        double n = (double) Math.max(this.x.length(), this.y.length());

        return (n-distance) / n;
    }

    public double distance(String xs, String ys) {
        x.set(xs);
        y.set(ys);

        return distance(x.start, y.start);
    }

    private double distance(int i, int j) {

        boolean eox = x.isEndOfWord(i);
        boolean eoy = y.isEndOfWord(j);

        if(eox && eoy) {
            return 0;

        } else if(eox) {
            return trailingDistance(y, j);

        } else if(eoy) {
            return trailingDistance(x, i);

        } else {

            double d = Double.POSITIVE_INFINITY;

//            if(x.isVowel(i) && y.isVowel(j)) {
//                double substitutionCost = nucleusDistance(i, j);
//            }

            double substitutionCost = substitutionDistance(i,j);
            if(substitutionCost < d) {
                substitutionCost += distance(i+1, j+1);
                d = min(d, substitutionCost);
            }

            double insertionCostX = insertionCost(x, i);
            if(insertionCostX < d) {
                insertionCostX += distance(i+1, j);
                d = min(d, insertionCostX);
            }

            double insertionCostY = insertionCost(y, j);
            if(insertionCostY < d) {
                insertionCostY += distance(i, j+1);
                d = min(d, insertionCostY);
            }

            return d;
        }
    }

    /**
     * Calculates the distance between the nuclei of two syllables
     */
    private double nucleusDistance(int i, int j) {
        int xn = 1;
        while(x.isVowel(i+xn)) {
            xn++;
        }
        int yn = 1;
        while(y.isVowel(i+yn)) {
            yn++;
        }

        return -1;
    }

    /**
     * Assign a cost to inserting the character at i in word {@code w}
     */
    private double insertionCost(Word w, int i) {

        char c = w.at(i);

        if(isVowelChar(c)) {
            return EXTRA_VOWEL_COST;

        } else if(c == 'Y') {
            if(w.isVowel(i-1)) {
                return 1.0;
            }

        } else /* consonants */ {
            if(w.isRepeated(i)) {
                return DOUBLED_CONSONANT_COST;
            }
            int next = w.at(i + 1);
            if(c == 'D' && next == 'J') {
                return 0.5;
            } else if(c == 'N' && (next == 'D' || next == 'G' || next == 'K')) {
                return 0.5;
            }
        }

        return Double.POSITIVE_INFINITY;
    }

    private double substitutionDistance(int i, int j) {
        char cx = x.at(i);
        char cy = y.at(j);

        if(cx == cy) {
            return 0;
        }

        boolean vx = isVowelChar(cx);
        boolean vy = isVowelChar(cy);

        if(vx && vy) {
            if(cx < cy) {
                return vowelDistance(cx, cy);
            } else {
                return vowelDistance(cy, cx);
            }
        } else if(!vx && !vy) {
            return consonants.distance(cx, cy);

        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    private double vowelDistance(char k, char m) {
        if(k == 'A' && (m == 'E')) {
            return 0.25;
        } else if(k == 'E' && (m == 'I')) {
            return 0.25;
        } else if(k == 'O' && (m == 'U')) {
            return 0.25;
        } else {
            return 1.0;
        }
    }


    /**
     * We consider extra letters largely to be infinitely far,
     * with the exception of extra vowels, which are often dropped
     * or
     */
    private double trailingDistance(Word word, int i) {
        while(!word.isEndOfWord(i)) {
            char c = word.at(i);

            if(!isVowelChar(c)) {
                if(word.isLast(i) && word.isVowel(i-1) &&
                        (c == 'H' || c == 'Y' || c == 'T')) {

                    return CLOSING_CONSONANT_COST;

                } else {

                    return Double.POSITIVE_INFINITY;
                }
            }
            i++;
        }
        return TRAILING_VOWELS_COST;
    }

}
