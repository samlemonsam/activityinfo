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

import com.google.gwt.core.shared.GwtIncompatible;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.activityinfo.model.query.ByteOrder;
import org.activityinfo.model.query.IntOrder;
import org.activityinfo.model.query.SortDir;

import java.util.BitSet;

/**
 * This utility heapsorts a column, reordering the given index arrays but leaving the value arrays unchanged.
 * Also allows for selection of a specific row range to sort on.
 */
public final class HeapsortColumn {

    /**
     * Combines a {@link ByteOrder} function with a sort direction. If {@code direction} is
     * {@link SortDir#DESC}, a new function is returned that reverses the ordering.
     */
    public static ByteOrder withDirection(ByteOrder order, SortDir direction) {
        if(direction == SortDir.ASC) {
            return order;
        } else {
            return (a, b) -> order.isLessThan(b, a);
        }
    }

    /**
     * Combines a {@link IntOrder} function with a sort direction. If {@code direction} is
     * {@link SortDir#DESC}, a new function is returned that reverses the ordering.
     */
    public static IntOrder withIntDirection(IntOrder order, SortDir direction) {
        if(direction == SortDir.ASC) {
            return order;
        } else {
            return (a, b) -> order.isLessThan(b, a);
        }
    }

    /**
     * Creates an {@link IntOrder} function that orders integers in their natural order.
     */
    public static IntOrder withIntDirection(SortDir direction) {
        if(direction == SortDir.ASC) {
            return (a, b) -> a < b;
        } else {
            return (a, b) -> b < a;
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
     * Given two numbers encoded as unsigned shorts from 0x0001-0xFFFF, with missing values encoded as zeroes
     *
     * @param sx
     * @param sy
     * @param ascending
     * @return
     */
    private static boolean isLessThan(short sx, short sy, boolean ascending) {
        // Treat as unsigned - promote to int with a 16-bit mask
        int x = sx & 0xFFFF;
        int y = sy & 0xFFFF;

        // Missing values encoded as zeroes
        boolean xMissing = (x == 0);
        boolean yMissing = (y == 0);

        if(ascending) {
            if(xMissing && !yMissing) {
                return true;
            }
            return x < y;
        } else {
            if(yMissing && !xMissing) {
                return true;
            }
            return y < x;
        }
    }


    private static boolean isLessThan(String str1, String str2, boolean ascending) {
        String a;
        String b;

        if (ascending) {
            a = toLower(str1);
            b = toLower(str2);
        } else {
            a = toLower(str2);
            b = toLower(str1);
        }

        if (a == null) {
            return b != null;
        } else if (b == null) {
            return false;
        } else {
            return a.compareTo(b) < 0;
        }
    }

    private static String toLower(String str) {
        if (str == null) {
            return null;
        } else {
            return str.toLowerCase();
        }
    }

    private static boolean isLessThan(boolean x, boolean y, boolean ascending) {
        boolean a;
        boolean b;

        if (ascending) {
            a = x;
            b = y;
        } else {
            a = y;
            b = x;
        }

        if (a == true) {
            return false;
        } else {
            return b == true;
        }
    }

    private static boolean isLessThan(Boolean x, Boolean y, boolean ascending) {
        Boolean a;
        Boolean b;

        if (ascending) {
            a = x;
            b = y;
        } else {
            a = y;
            b = x;
        }

        if (a == Boolean.TRUE) {
            return false;
        } else if (a == Boolean.FALSE) {
            return b == Boolean.TRUE;
        } else {
            return b != null;
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
    public static void heapsortCompact16(short[] val, int[] index, int n, int[] range, boolean ascending) {
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
    public static void heapsortCompact16(short[] val, int[] index, int n, boolean ascending) {
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
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}), using the provided order function
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     * @param order a function which provides an ordering over the integers
     */
    public static void heapsortInt(int[] val, int[] index, int n, int[] range, IntOrder order) {
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
                if (j < ir && order.isLessThan(val[index[range[j]]], val[index[range[j+1]]]) || i == j) {
                    ++j;
                }
                if (order.isLessThan(ra, val[index[range[j]]])) {
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
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}), using the provided order function.
     *
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     */
    public static void heapsortInt(int[] val, int[] index, int n, IntOrder ordering) {
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
                if (j < ir && ordering.isLessThan(val[index[j]], val[index[j+1]]) || i == j) {
                    ++j;
                }
                if (ordering.isLessThan(ra, val[index[j]])) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     * @param ascending
     */
    public static void heapsortDouble(double[] val, int[] index, int n, int[] range, boolean ascending) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     * @param ascending
     */
    public static void heapsortDouble(double[] val, int[] index, int n, boolean ascending) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     * @param ascending
     */
    public static void heapsortString(String[] val, int[] index, int n, int[] range, boolean ascending) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     * @param ascending
     */
    public static void heapsortString(String[] val, int[] index, int n, boolean ascending) {
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
    public static void heapsortByte(byte[] val, int[] index, int n, int[] range, ByteOrder order) {
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
                if (j < ir && order.isLessThan(val[index[range[j]]], val[index[range[j+1]]]) || i == j) {
                    ++j;
                }
                if (order.isLessThan(ra, val[index[range[j]]])) {
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
     * <p>Sorts a column index vector ({@code index}) by row values ({@code val}) using the heapsort
     * algorithm. The row value array is not mutated during sorting. </p>
     * @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     * @param order a function providing and ordering between bytes
     */
    public static void heapsortByte(byte[] val, int[] index, int n, ByteOrder order) {
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
                if (j < ir && order.isLessThan(val[index[j]], val[index[j+1]]) || i == j) {
                    ++j;
                }
                if (order.isLessThan(ra, val[index[j]])) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     * @param ascending
     */
    @GwtIncompatible
    public static void heapsortSparseDouble(Int2DoubleOpenHashMap val, int[] index, int n, int[] range, boolean ascending) {
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
                ra = val.get(ii);
            }

            else {
                ii = index[range[ir]];
                ra = val.get(ii);
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val.get(index[range[j]]), val.get(index[range[j+1]]), ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val.get(index[range[j]]), ascending)) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     * @param ascending
     */
    @GwtIncompatible
    public static void heapsortSparseDouble(Int2DoubleOpenHashMap val, int[] index, int n, boolean ascending) {
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
                ra = val.get(ii);
            }

            else {
                ii = index[ir];
                ra = val.get(ii);
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val.get(index[j]), val.get(index[j+1]), ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val.get(index[j]), ascending)) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     * @param ascending
     */
    public static void heapsortBitSet(BitSet val, int[] index, int n, int[] range, boolean ascending) {
        int l, j, ir, i;
        boolean ra;
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
                ra = val.get(ii);
            }

            else {
                ii = index[range[ir]];
                ra = val.get(ii);
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val.get(index[range[j]]), val.get(index[range[j+1]]), ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val.get(index[range[j]]), ascending)) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     * @param ascending
     */
    public static void heapsortBitSet(BitSet val, int[] index, int n, boolean ascending) {
        int l, j, ir, i;
        boolean ra;
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
                ra = val.get(ii);
            }

            else {
                ii = index[ir];
                ra = val.get(ii);
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(val.get(index[j]), val.get(index[j+1]), ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, val.get(index[j]), ascending)) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index} - should be equal to the size of {@code range}
     * @param range The rows of {@code val} on which to sort (unmutated)
     * @param ascending
     */
    public static void heapsortBitSetMissing(BitSet val, BitSet missing, int[] index, int n, int[] range, boolean ascending) {
        int l, j, ir, i;
        Boolean ra;
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
                ra = maybeMissingVal(val, missing, ii);
            }

            else {
                ii = index[range[ir]];
                ra = maybeMissingVal(val, missing, ii);
                index[range[ir]] = index[range[0]];

                if (--ir == 0) {
                    index[range[0]] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(maybeMissingVal(val, missing, index[range[j]]), maybeMissingVal(val, missing, index[range[j+1]]), ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, maybeMissingVal(val, missing, index[range[j]]), ascending)) {
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
     *  @param val The value array to sort (unmutated)
     * @param index The index array to sort in tandem (mutated). This array gives the current order indices of {@code val}
     * @param n The length of {@code val} and {@code index}
     * @param ascending
     */
    public static void heapsortBitSetMissing(BitSet val, BitSet missing, int[] index, int n, boolean ascending) {
        int l, j, ir, i;
        Boolean ra;
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
                ra = maybeMissingVal(val, missing, ii);
            }

            else {
                ii = index[ir];
                ra =maybeMissingVal(val, missing, ii);
                index[ir] = index[0];

                if (--ir == 0) {
                    index[0] = ii;
                    return; // We're done
                }
            }

            i = l;
            j = (l << 1);
            while (j <= ir) {
                if (j < ir && isLessThan(maybeMissingVal(val, missing, index[j]), maybeMissingVal(val, missing, index[j+1]), ascending) || i == j) {
                    ++j;
                }
                if (isLessThan(ra, maybeMissingVal(val, missing, index[j]), ascending)) {
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

    private static Boolean maybeMissingVal(BitSet values, BitSet missing, int row) {
        if (missing.get(row)) {
            return null;
        } else {
            return values.get(row);
        }
    }

}