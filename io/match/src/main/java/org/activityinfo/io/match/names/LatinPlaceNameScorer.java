package org.activityinfo.io.match.names;

import com.google.common.annotations.VisibleForTesting;

import static java.lang.Math.max;

/**
 * Scores the similarity between two names names written in a Latin
 * script.
 *
 */
public class LatinPlaceNameScorer {


    public static final int MINIMUM_STRING_LENGTH_FOR_FUZZY_MATCHING = 1;

    private static final int NONE = -1;

    // We store the current names names that we're matching against
    // in a pair of flyweight classes to void creating bazillions
    // of little objects in the course of matching

    private final LatinPlaceName x = new LatinPlaceName();
    private final LatinPlaceName y = new LatinPlaceName();

    private final LatinWordDistance distanceFunction = new LatinWordDistance();

    // Use a flyweight array for tracking permutations
    private final int permutation[] = new int[LatinPlaceName.MAX_PARTS];

    public LatinPlaceNameScorer() {
    }

    public double score(String importedValue, String referencedValue) {

        // quick check...
        if(importedValue.equals(referencedValue)) {
            return 1.0;
        }

        // if we don't have an exact match, first normalize the
        // the strings into lists of lowercase parts free of
        // diacriticals or other messiness

        x.set(importedValue);
        y.set(referencedValue);


        if(x.isEmpty() || y.isEmpty()) {
            return 0.0;
        }

        // Now we have two sets of ordered components, for example:
        //  [T1, T2, T3]  and  [S1, S2]

        // we want to know how likely it is that S refers to the
        // same entity as T.

        // Names are funny things and subject to a great deal of
        // violence by humans. There are a number of things that
        // can happen to a written name:

        // (1) The same sound may be written differently, perhaps because
        //     of a different transliteration scheme, for example
        //
        //       [ou]adi => [w]adi
        //       zou[q] bha[nn]ine => zou[k] bha[n]ine
        //       z[ai]toun => z[ei]toun[e]
        //
        // (2) Sounds can drift regionally and over time, and these
        //     differences result in new spellings
        //
        //
        // (3) Names that include parts of speech can be reordered or
        //     discarded arbitrarily
        //
        //     [santa, rosa, city] => [city, of, santa, rosa]
        //     [commune, de, goumera] => [goumera]
        //
        // (4) Words can be split or joined
        //
        //     [bara, sara] => [barassara]
        //     [nema, badenyakafo] => [nema, badenya, kafo]
        //

        // So we not only have to deal with fuzzy matching, but we have to deal with it
        // on multiple levels:

        // (1) Proportion of parts matching
        // (2) Combinations of parts
        // (3) Similarity between non-matching parts

        // So we start off by running through all the combinations. If we have
        //  [A, B, C]  and  [X, Y]

        // We have to expand this into a set of mergings
        // [A, B, C]         [X, Y]
        // [AB,   C]   x     [XY]
        // [A,   BC]

        // We can think of each break between parts as a bit, and since
        // we're only concerned with names names composed of a small
        // number of parts, we'll use a bit set to iterate through all
        // the combinations

        // first try with no merging

        double score = findBestPermutationScore();


        return score;

    }

    @VisibleForTesting
    void init(String xs, String ys) {
        this.x.set(xs);
        this.y.set(ys);
    }

    /**
     * Given two names names like X = "COMMUNE DE KAYES" and Y = "KAYES COMMUNE",
     * score the similarity between each part X[i] and Y[j] and find the best
     * assignment from i -> j.
     *
     * <ul>
     *     <li>score([KAYES, COMMUNE], [COMMUNE, DE])</li>
     *     <li>score([KAYES, COMMUNE], [COMMUNE, KAYES])</li>
     *     <li>score([KAYES, COMMUNE], [DE, KAYES]</li>
     *     <li>score([KAYES, COMMUNE], [KAYES, COMMUNE]</li>
     *     <li>etc</li>
     * </ul>
     * @return
     */
    @VisibleForTesting
    double findBestPermutationScore() {

        // swap x and y if necessary so that
        // left.numParts <= right.numParts

        LatinPlaceName left, right;
        if(x.partCount() <= y.partCount()) {
            left = this.x;
            right = this.y;
        } else {
            left = this.y;
            right = this.x;
        }

        // find the similarity between each pair of parts in left and right

        int leftParts = left.partCount();
        int rightParts = right.partCount();

        double scores[][] = new double[leftParts][rightParts];
        for(int i=0;i<leftParts;++i) {
            for(int j=0;j<rightParts;++j) {
                scores[i][j] = similarity(left, i, right, j);
            }
        }

        // now find the best partial permutation among the right name parts, taking
        // the score
        double bestScore = 0;

        // loop through each partial permutation of the right parts
        PartialPermutations.first(permutation, rightParts);
        do {
            double numerator = 0;
            double denominator = 0;

            // keep track of the parts from the right that
            // are not included in this permutation, they
            // need to be included in the denominator
            double extraRightParts = right.charCount();

            for(int leftPart=0;leftPart<leftParts;++leftPart) {
                int rightPart = permutation[leftPart];

                double score = scores[leftPart][rightPart];

                int leftLength = left.charCount(leftPart);
                int rightLength = right.charCount(rightPart);

                // we use the minimum length of the word as the weight
                // to inflating short words that match longer words because
                // of lots of vowels
                int minLength = Math.min(leftLength, rightLength);
                numerator += score * (double)minLength;

                if(score > 0.0) {
                    denominator += minLength;
                } else {
                    denominator += leftLength + rightLength;
                }

                extraRightParts -= rightLength;
            }

            denominator += extraRightParts;

            bestScore = max(bestScore, numerator / denominator);

        } while(PartialPermutations.next(permutation, rightParts, leftParts));

       return bestScore;
    }

    private double similarity(LatinPlaceName left, int leftPartIndex, LatinPlaceName right, int rightPartIndex) {
        int leftChars = left.charCount(leftPartIndex);
        int rightChars = right.charCount(rightPartIndex);

        // first try an exact comparison
        if(leftChars == rightChars) {
            boolean matchesExactly = true;
            for(int i=0;i!=leftChars;++i) {
                if(left.charAt(leftPartIndex, i) != right.charAt(rightPartIndex, i)) {
                    matchesExactly = false;
                    break;
                }
            }
            if(matchesExactly) {
                return 1.0;
            }
        }

        boolean numericLeft = left.isPartNumeric(leftPartIndex);
        boolean numericRight = right.isPartNumeric(rightPartIndex);

        if(numericLeft && numericRight) {
            if(left.parsePartAsInteger(leftPartIndex) == right.parsePartAsInteger(rightPartIndex)) {
                return 1.0;
            } else {
                return 0.0;
            }
        } else if(numericLeft) {
            return tryCompareNumericWithAlpha(left, leftPartIndex, right, rightPartIndex);

        } else if(numericRight) {
            return tryCompareNumericWithAlpha(right, rightPartIndex, left, leftPartIndex);
        }

        // now try an approximate match based on the phonetic shape

        return distanceFunction.similarity(
                left.chars, left.partStart(leftPartIndex), left.partStart(leftPartIndex + 1),
                right.chars, right.partStart(rightPartIndex), right.partStart(rightPartIndex + 1));
    }

    private double tryCompareNumericWithAlpha(LatinPlaceName nameWithNumericPart, int numericPartIndex,
                                              LatinPlaceName nameWithAlphaPart, int alphaPartIndex) {

        int x = nameWithNumericPart.parsePartAsInteger(numericPartIndex);
        int y = nameWithAlphaPart.tryParsePartAsRomanNumeral(alphaPartIndex);

        if(x == y) {
            return 1.0;
        } else {
            return 0.0;
        }
    }



}
