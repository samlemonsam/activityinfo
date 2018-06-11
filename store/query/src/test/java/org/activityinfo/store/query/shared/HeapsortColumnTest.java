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
package org.activityinfo.store.query.shared;

import org.activityinfo.model.query.*;
import org.activityinfo.store.query.server.columns.DiscreteStringColumnView8;
import org.activityinfo.store.query.server.columns.IntColumnView16;
import org.activityinfo.store.query.server.columns.IntColumnView8;
import org.activityinfo.store.query.server.columns.SparseNumberColumnView;
import org.activityinfo.store.query.shared.columns.DiscreteStringColumnView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;

import static org.junit.Assert.assertThat;

public class HeapsortColumnTest {

    @Test
    public void int8sorting() {

        double values[] = new double[] {0, Double.NaN, 50, 30, -33 };
        IntColumnView8 columnView = new IntColumnView8(values, values.length, -33);
        int[] indexes = new int[] { 0, 1, 2, 3, 4 };

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(Double.NaN, -33, 0, 30, 50));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(50, 30, 0, -33, Double.NaN));
    }

    @Test
    public void int16sorting() {
        double values[] = new double[] {0, Double.NaN, 500, 300, -330 };
        IntColumnView16 columnView = new IntColumnView16(values, values.length, -330);
        int[] indexes = new int[] { 0, 1, 2, 3, 4 };

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(Double.NaN, -330, 0, 300, 500));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(500, 300, 0, -330, Double.NaN));
    }

    @Test
    public void doubleSorting() {
        double values[] = { Double.NaN, 51.0, -15.0, 5.1, -1.5, Double.NaN, 510.0, -150.0, 0.0 };
        int indexes[] = {0, 1, 2, 3, 4, 5, 6, 7, 8 };
        DoubleArrayColumnView columnView = new DoubleArrayColumnView(values, values.length);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(Double.NaN, Double.NaN, -150.0, -15.0, -1.5, 0.0, 5.1, 51.0, 510.0));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(510.0, 51.0, 5.1, 0, -1.5, -15.0, -150.0, Double.NaN, Double.NaN));
    }

    @Test
    public void sparseDoubleSorting() {
        double values[] = { Double.NaN, Double.NaN, Double.NaN, 1.0, -1.0, Double.NaN, 2.0, Double.NaN, 0.0 };
        int indexes[] = {0, 1, 2, 3, 4, 5, 6, 7, 8 };
        SparseNumberColumnView columnView = new SparseNumberColumnView(values, 9, 5);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, -1.0, 0.0, 1.0, 2.0));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(2.0, 1.0, 0.0, -1.0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN));
    }

    @Test
    public void booleanSorting() {
        int values[] = new int[] {0, ColumnView.NA, 1, 1, 0, ColumnView.NA };
        int[] indexes = new int[] { 0, 1, 2, 3, 4, 5 };
        BooleanColumnView columnView = new BooleanColumnView(values);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(ColumnView.NA, ColumnView.NA, 0, 0, 1, 1));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(1, 1, 0, 0, ColumnView.NA, ColumnView.NA));
    }

    @Test
    public void stringSorting() {
        String values[] = {"Abc", "aaa", "Cba", "", "ccc", "a", "c", null};
        int indexes[] = {0, 1, 2, 3, 4, 5, 6, 7};
        StringArrayColumnView columnView = new StringArrayColumnView(values);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(null, "", "a", "aaa", "Abc", "c", "Cba", "ccc"));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo("ccc", "Cba", "c", "Abc", "aaa", "a", "", null));
    }

    @Test
    public void enumSorting() {
        String labels[] = {"a", "b", "c", ""};
        int selections[] = {2, 1, 0, -1, 3, 1}; // c, b, a, null, "", b
        int indexes[] = {0, 1, 2, 3, 4, 5};
        DiscreteStringColumnView columnView = new DiscreteStringColumnView(labels, selections);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(labelledArray(reorder(selections, indexes), labels), isArrayEqualTo(null, "", "a", "b", "b", "c"));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(labelledArray(reorder(selections, indexes), labels), isArrayEqualTo("c", "b", "b", "a", "", null));
    }

    @Test
    public void enum8Sorting() {
        String labels[] = {"a", "b", "c", ""};
        byte selections[] = {2, 1, 0, -1, 3, 1}; // c, b, a, null, "", b
        int indexes[] = {0, 1, 2, 3, 4, 5};
        DiscreteStringColumnView8 columnView = new DiscreteStringColumnView8(labels, selections);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(labelledArray(reorder(selections, indexes), labels), isArrayEqualTo(null, "", "a", "b", "b", "c"));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(labelledArray(reorder(selections, indexes), labels), isArrayEqualTo("c", "b", "b", "a", "", null));
    }

    @Test
    public void bitSetSorting() {
        BitSet values = new BitSet(4);
        values.set(1, true);
        values.set(2, false);
        values.set(3, true);
        values.set(4, false);
        int indexes[] = {0, 1, 2, 3};
        BitSetColumnView columnView = new BitSetColumnView(values.length(), values);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(false, false, true, true));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(true, true, false, false));
    }

    @Test
    public void bitSetMissingSorting() {
        BitSet values = new BitSet(5);
        values.set(1, true);
        values.set(2, false);
        values.set(4, true);
        values.set(5, false);
        BitSet missing = new BitSet(5);
        missing.set(1, false);
        missing.set(2, false);
        missing.set(3, true);
        missing.set(4, false);
        missing.set(5, false);
        int indexes[] = {0, 1, 2, 3, 4};
        BitSetWithMissingView columnView = new BitSetWithMissingView(values.length(), values, missing);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, missing, indexes), isBooleanArrayEqualTo(null, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, missing, indexes), isBooleanArrayEqualTo(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null));
    }

//    @Test
//    public void multipleSort() {
//        int masterRowId[] = {0, 1, 2, 3, 4, 5, 6};
//        int sortVector[] = masterRowId.clone();
//        String stringCol[] = {"C", "D", "A", "E", "B", "C", "C"};
//        double doubleCol[] = {15, 17, 12, 13, 12, 12, 11};
//
//        SortModel.Range range = new SortModel.Range(0,6);
//
//        System.out.println("Original Ids: " + Arrays.toString(masterRowId));
//        System.out.println("Sort Vector:  " + Arrays.toString(sortVector));
//        System.out.println("String Col:   " + Arrays.toString(stringCol));
//        System.out.println("Double Col:   " + Arrays.toString(doubleCol));
//
//        HeapsortColumn.heapsortAscending(stringCol, sortVector, range.getRangeSize(), range.getRange());
//
//        System.out.println("1st Sort on String Col");
//        System.out.println("Original Ids:             " + Arrays.toString(masterRowId));
//        System.out.println("Sort Vector:              " + Arrays.toString(sortVector));
//        System.out.println("String Col:               " + Arrays.toString(stringCol));
//        System.out.println("String Col (Reordered):   " + Arrays.toString(reorder(stringCol, sortVector)));
//        System.out.println("Double Col:               " + Arrays.toString(doubleCol));
//        System.out.println("Double Col (Reordered):   " + Arrays.toString(reorder(doubleCol, sortVector)));
//
//        range = new SortModel.Range(2,4);
//
//        //HeapsortColumn.heapsortAscending(doubleCol, sortVector, range.getRangeSize(), range.getRange());
//
//        System.out.println("2nd Sort on Double Col");
//        System.out.println("Original Ids:             " + Arrays.toString(masterRowId));
//        System.out.println("Sort Vector:              " + Arrays.toString(sortVector));
//        System.out.println("String Col:               " + Arrays.toString(stringCol));
//        System.out.println("String Col (Reordered):   " + Arrays.toString(reorder(stringCol, sortVector)));
//        System.out.println("Double Col:               " + Arrays.toString(doubleCol));
//        System.out.println("Double Col (Reordered):   " + Arrays.toString(reorder(doubleCol, sortVector)));
//
//    }

    private String[] labelledArray(int[] selection, String[] labels) {
        String[] output = new String[selection.length];
        for (int i=0; i < output.length; i++) {
            output[i] = selection[i] < 0 ? null : labels[selection[i]];
        }
        return output;
    }

    private String[] labelledArray(byte[] selection, String[] labels) {
        String[] output = new String[selection.length];
        for (byte i=0; i < output.length; i++) {
            output[i] = selection[i] < 0 ? null : labels[selection[i]];
        }
        return output;
    }

    private int[] reorder(int[] original, int[] sortVector) {
        int[] output = new int[original.length];
        for (int i=0; i<output.length; i++) {
            output[i] = original[sortVector[i]];
        }
        return output;
    }

    private byte[] reorder(byte[] original, int[] sortVector) {
        byte[] output = new byte[original.length];
        for (byte i=0; i<output.length; i++) {
            output[i] = original[sortVector[i]];
        }
        return output;
    }

    private Object[] reorder(Object[] original, int[] sortVector) {
        Object[] output = new Object[original.length];
        for (int i=0; i<output.length; i++) {
            output[i] = original[sortVector[i]];
        }
        return output;
    }

    private String[] reorder(String[] original, int[] sortVector) {
        String[] output = new String[original.length];
        for (int i=0; i<output.length; i++) {
            output[i] = original[sortVector[i]];
        }
        return output;
    }

    private double[] reorder(double[] original, int[] sortVector) {
        double[] output = new double[original.length];
        for (int i=0; i<output.length; i++) {
            output[i] = original[sortVector[i]];
        }
        return output;
    }

    private boolean[] reorder(BitSet original, int[] sortVector) {
        boolean[] output = new boolean[original.length()];
        for (int i=0; i<output.length; i++) {
            output[i] = original.get(sortVector[i]);
        }
        return output;
    }

    private Boolean[] reorder(BitSet original, BitSet missing, int[] sortVector) {
        Boolean[] output = new Boolean[original.length()];
        for (int i=0; i<output.length; i++) {
            output[i] = missing.get(sortVector[i]) ? null : original.get(sortVector[i]);
        }
        return output;
    }

    private static Matcher<double[]> isArrayEqualTo(double... expected) {
        return new TypeSafeMatcher<double[]>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("array equal to ");
                description.appendText(Arrays.toString(expected));
            }

            @Override
            protected boolean matchesSafely(double[] item) {
                return Arrays.equals(expected, item);
            }
        };
    }

    private static Matcher<int[]> isArrayEqualTo(int... expected) {
        return new TypeSafeMatcher<int[]>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("array equal to ");
                description.appendText(Arrays.toString(expected));
            }

            @Override
            protected boolean matchesSafely(int[] item) {
                return Arrays.equals(expected, item);
            }
        };
    }

    private static Matcher<String[]> isArrayEqualTo(String... expected) {
        return new TypeSafeMatcher<String[]>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("array equal to ");
                description.appendText(Arrays.toString(expected));
            }

            @Override
            protected boolean matchesSafely(String[] item) {
               return Arrays.equals(expected, item);
            }
        };
    }

    private static Matcher<boolean[]> isArrayEqualTo(boolean... expected) {
        return new TypeSafeMatcher<boolean[]>() {
            @Override
            protected boolean matchesSafely(boolean[] item) {
                return Arrays.equals(expected, item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("array equal to ");
                description.appendText(Arrays.toString(expected));
            }
        };
    }

    private static Matcher<Boolean[]> isBooleanArrayEqualTo(Boolean... expected) {
        return new TypeSafeMatcher<Boolean[]>() {
            @Override
            protected boolean matchesSafely(Boolean[] item) {
                return Arrays.equals(expected, item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("array equal to ");
                description.appendText(Arrays.toString(expected));
            }
        };
    }
}