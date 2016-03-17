/**
 * jcombinatorics:
 * Java Combinatorics Library
 *
 * Copyright (c) 2009 by Alistair A. Israel.
 *
 * This software is made available under the terms of the MIT License.
 * See LICENSE.txt.
 *
 * Created Sep 2, 2009
 */
package org.activityinfo.io.match.names;


/**
 * <p>
 * An iterator that enumerates <code>P(n,k)</code>, or all permutations of
 * <code>n</code> taken <code>k</code> at a time in lexicographic order. Derived
 * from the SEPA P(n) iterator.
 * </p>
 *
 * @author Alistair A. Israel, Java Combinatorics Library
 * @see <a href="https://github.com/AlistairIsrael/jcombinatorics/blob/7af141f9e48e09ada3d6bde2f038fd71013ba7d4/src/main/java/jcombinatorics/permutations/SepaPnIterator.java">
 *     Original Source</a>
 */
public final class PartialPermutations {


    /**
     * Initializes an array {@code a} with the first permutation in a lexicographic sequence.
     *
     * Fills the given array with a[i] = i. For example, if a = int[4], then
     * fills <code>a</code> with <code>{ 0, 1, 2, 3 }</code>.
     *
     * @param a an array
     * @param n the length of the partial permutation
     */
    public static void first(final int[] a, int n) {
        for (int i = n - 1; i >= 0; --i) {
            a[i] = i;
        }
    }

    public static int[] identity(int n) {
        int[] a = new int[n];
        first(a, n);
        return a;
    }


    /**
     * Computes the next partial permutation P(n, k) in a lexicographic
     * sequence of partial permutations.
     * @param a an array of length >= {@code n} holding the current permutation.
     * @param n the number of elements
     * @param k taken k at a time
     */
    public static boolean next(int[] a, int n, int k) {
        assert a.length >= n : "a must have a length >= n";

        int i = k - 1;
        int j = k;
        // find smallest j > k - 1 where a[j] > a[k - 1]
        while (j < n && a[i] >= a[j]) {
            ++j;
        }
        if (j < n) {
            swap(a, i, j);
        } else {
            reverseRightOf(a, i, n);
            // i = (k - 1) - 1
            --i;
            while (i >= 0 && a[i] >= a[i + 1]) {
                --i;
            }
            if (i < 0) {
                return false;
            }
            // j = n - 1
            --j;
            while (j > i && a[i] >= a[j]) {
                --j;
            }
            swap(a, i, j);
            reverseRightOf(a, i, n);
        }
        return true;
    }

    /**
     * Reverse the order of elements from <code>a[start + 1]..a[n-1]</code>.
     *
     * @param a
     * @param start the starting element
     * @param n the number of elements, {@code >= k}
     */
    private static void reverseRightOf(int[] a, final int start, int n) {
        int i = start + 1;
        int j = n - 1;
        while (i < j) {
            swap(a, i, j);
            ++i;
            --j;
        }
    }

    /**
     * Swaps two elements in an array
     *
     * @param a
     * @param x  first position
     * @param y  second position
     */
    private static void swap(int[] a, final int x, final int y) {
        final int t = a[x];
        a[x] = a[y];
        a[y] = t;
    }

}