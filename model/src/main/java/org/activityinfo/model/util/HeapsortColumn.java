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

package org.activityinfo.model.util;

import com.google.common.primitives.UnsignedBytes;

/**
 * This utility heapsorts a column, reordering the given index arrays but leaving the value arrays unchanged.
 * Also allows for selection of a specific row range to sort on.
 * Derived from the {@link HeapsortTandem} class.
 */
public final class HeapsortColumn {

    private static int compare(String str1, String str2) {
        if (str1 == str2) {
            return 0;
        } else if (str1 == null) {
            return -1;
        } else if (str2 == null) {
            return 1;
        } else {
            return str1.compareTo(str2);
        }
    }

    private static int compare(String[] labels, int enum1, int enum2) {
        if (enum1 == enum2) {
            return 0;
        } else if (enum1 == -1) {
            return -1;
        } else if (enum2 == -1) {
            return 1;
        } else {
            return labels[enum1].compareTo(labels[enum2]);
        }
    }

    private static int compare(String[] labels, byte enum1, byte enum2) {
        if (enum1 == enum2) {
            return 0;
        } else if (enum1 == -1) {
            return -1;
        } else if (enum2 == -1) {
            return 1;
        } else {
            return labels[enum1].compareTo(labels[enum2]);
        }
    }


    private static boolean isLessThan(double x, double y, boolean ascending) {
        if(ascending) {
            if (x < y) {
                return true;
            }
            if (Double.isNaN(x) && !Double.isNaN(y)) {
                return true;
            }
            return false;

        } else {
            if(y < x) {
                return true;
            }
            if (Double.isNaN(y) && !Double.isNaN(x)) {
                return true;
            }
            return false;
        }
    }

    /**
     * Given two numbers encoded using
     * @param x
     * @param y
     * @param ascending
     * @return
     */
    private static boolean isLessThan(byte bx, byte by, boolean ascending) {
        // Treat as unsigned
        int x = UnsignedBytes.toInt(bx);
        int y = UnsignedBytes.toInt(by);
        if(ascending) {
            if(x == 0 && y != 0) {
                return true;
            }
            return UnsignedBytes.toInt(bx) < UnsignedBytes.toInt(by);

        } else {
            if(y == 0 && x != 0) {
                return true;
            }
            return y < x;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) encoded as unsigned 8-bit integers,
     * with zero as the missing value.
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortCompact8(byte[] val, int[] index, int n, int[] range, boolean ascending) {
        int l, j, ir, i;
        byte ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val[index[range[j]]], val[index[range[j+1]]], ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val[index[range[j]]], ascending)) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) encoded as unsigned 8-bit integers,
     * with zero as the missing value.
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortCompact8(byte[] val, int[] index, int n, boolean ascending) {
        int l, j, ir, i;
        byte ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val[index[j]], val[index[j+1]], ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val[index[j]], ascending)) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }



    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortAscending(int[] val, int[] index, int n, int[] range) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && val[index[range[j]]] < val[index[range[j+1]]] || i == j) {
                    ++j;
                }
                if (ra < val[index[range[j]]]) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortAscending(int[] val, int[] index, int n) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && val[index[j]] < val[index[j+1]] || i == j) {
                    ++j;
                }
                if (ra < val[index[j]]) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortDescending(int[] val, int[] index, int n, int[] range) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && val[index[range[j]]] > val[index[range[j+1]]] || i == j) {
                    ++j;
                }
                if (ra > val[index[range[j]]]) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortDescending(int[] val, int[] index, int n) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && val[index[j]] > val[index[j+1]] || i == j) {
                    ++j;
                }
                if (ra > val[index[j]]) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }



    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortAscending(short[] val, int[] index, int n, int[] range) {
        int l, j, ir, i;
        short ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && val[index[range[j]]] < val[index[range[j+1]]] || i == j) {
                    ++j;
                }
                if (ra < val[index[range[j]]]) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortAscending(short[] val, int[] index, int n) {
        int l, j, ir, i;
        short ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && val[index[j]] < val[index[j+1]] || i == j) {
                    ++j;
                }
                if (ra < val[index[j]]) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortDescending(short[] val, int[] index, int n, int[] range) {
        int l, j, ir, i;
        short ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && val[index[range[j]]] > val[index[range[j+1]]] || i == j) {
                    ++j;
                }
                if (ra > val[index[range[j]]]) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortDescending(short[] val, int[] index, int n) {
        int l, j, ir, i;
        short ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val[index[j+1]], val[index[j]], true) || i == j) {
                    ++j;
                }
                if (ra > val[index[j]]) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }


    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortAscending(double[] val, int[] index, int n, int[] range) {
        heapsort(val, index, n, range, true);
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     * @param ascending
     */
    public static void heapsort(double[] val, int[] index, int n, int[] range, boolean ascending) {
        int l, j, ir, i;
        double ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val[index[range[j]]], val[index[range[j+1]]], ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val[index[range[j]]], ascending)) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }


    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortAscending(double[] val, int[] index, int n) {
        heapsort(val, index, n, true);
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     * @param ascending
     */
    private static void heapsort(double[] val, int[] index, int n, boolean ascending) {
        int l, j, ir, i;
        double ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val[index[j]], val[index[j+1]], ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val[index[j]], ascending)) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortDescending(double[] val, int[] index, int n, int[] range) {
        heapsort(val, index, n, range, false);
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortDescending(double[] val, int[] index, int n) {
        heapsort(val, index, n, false);
    }



    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortAscending(String[] val, int[] index, int n, int[] range) {
        int l, j, ir, i;
        String ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(val[index[range[j]]], val[index[range[j+1]]]) < 0 || i == j) {
                    ++j;
                }
                if (compare(ra, val[index[range[j]]]) < 0) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortAscending(String[] val, int[] index, int n) {
        int l, j, ir, i;
        String ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(val[index[j]], val[index[j+1]]) < 0 || i == j) {
                    ++j;
                }
                if (compare(ra, val[index[j]]) < 0) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortDescending(String[] val, int[] index, int n, int[] range) {
        int l, j, ir, i;
        String ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(val[index[range[j]]], val[index[range[j+1]]]) > 0 || i == j) {
                    ++j;
                }
                if (compare(ra, val[index[range[j]]]) > 0) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortDescending(String[] val, int[] index, int n) {
        int l, j, ir, i;
        String ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(val[index[j]], val[index[j+1]]) > 0 || i == j) {
                    ++j;
                }
                if (compare(ra, val[index[j]]) > 0) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }



    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortEnumAscending(int[] val, String[] labels, int[] index, int n, int[] range) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[range[j]]], val[index[range[j+1]]]) < 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[range[j]]]) < 0) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortEnumAscending(int[] val, String[] labels, int[] index, int n) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[j]], val[index[j+1]]) < 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[j]]) < 0) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortEnumDescending(int[] val, String[] labels, int[] index, int n, int[] range) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[range[j]]], val[index[range[j+1]]]) > 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[range[j]]]) > 0) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortEnumDescending(int[] val, String[] labels, int[] index, int n) {
        int l, j, ir, i;
        int ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[j]], val[index[j+1]]) > 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[j]]) > 0) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }



    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortEnumAscending(byte[] val, String[] labels, int[] index, int n, int[] range) {
        int l, j, ir, i;
        byte ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[range[j]]], val[index[range[j+1]]]) < 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[range[j]]]) < 0) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortEnumAscending(byte[] val, String[] labels, int[] index, int n) {
        int l, j, ir, i;
        byte ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[j]], val[index[j+1]]) < 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[j]]) < 0) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     * <p>Constricts the rows of {@code val}/{@code index} to be sorted to the indices given by {@code range}.</p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     */
    public static void heapsortEnumDescending(byte[] val, String[] labels, int[] index, int n, int[] range) {
        int l, j, ir, i;
        byte ra;
        int ii;

        if (n <= 1 || n != range.length) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[range[l]];
                ra = val[ii];
            }

            else {
                ii = index[range[ir]];
                ra = val[ii];
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[range[j]]], val[index[range[j+1]]]) > 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[range[j]]]) > 0) {
                    index[range[i]] = index[range[j]];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[range[i]] = ii;
        }
    }

    /**
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using using GNU R's 'revsort' heapsort
     * algorithm ( sort.c ).The row value array is not mutated during sorting. </p>
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortEnumDescending(byte[] val, String[] labels, int[] index, int n) {
        int l, j, ir, i;
        byte ra;
        int ii;

        if (n <= 1) {
            return;
        }

        l = (n >> 1) + 1;
        ir = n-1;

        while(true) {
            // ==================================================
            // Heapify a and ib and then sort them
            // ==================================================

            // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
            if (l > 0) {
                l = l - 1;
                ii = index[l];
                ra = val[ii];
            }

            else {
                ii = index[ir];
                ra = val[ii];
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && compare(labels, val[index[j]], val[index[j+1]]) > 0 || i == j) {
                    ++j;
                }
                if (compare(labels, ra, val[index[j]]) > 0) {
                    index[i] = index[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }

            index[i] = ii;
        }
    }



}