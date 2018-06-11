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

import org.activityinfo.model.query.BooleanColumnView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.query.SortModel;
import org.activityinfo.model.util.HeapsortColumn;
import org.activityinfo.store.query.server.columns.IntColumnView16;
import org.activityinfo.store.query.server.columns.IntColumnView8;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertThat;

public class HeapsortColumnTest {

    @Test
    public void stringColumnSortedByValueAsc() {

        String stringVals[] = {"c", "a", "b", "s"};
        int stringIndex[] = {0, 1, 2, 3};

        System.out.println(Arrays.toString(stringVals));
        System.out.println(Arrays.toString(stringIndex));

        SortModel.Range range = new SortModel.Range(0,3);
        HeapsortColumn.heapsortAscending(stringVals, stringIndex, range.getRangeSize(), range.getRange());

        System.out.println("Original  (A): " + Arrays.toString(stringVals));
        System.out.println("Reordered (A): " + Arrays.toString(reorder(stringVals, stringIndex)));
        System.out.println("Index     (A): " + Arrays.toString(stringIndex));

    }

    @Test
    public void stringColumnSortedByValueDsc() {

        String stringVals[] = {"c", "a", "b", "s"};
        int stringIndex[] = {0, 1, 2, 3};

        System.out.println(Arrays.toString(stringVals));
        System.out.println(Arrays.toString(stringIndex));

        SortModel.Range range = new SortModel.Range(0,3);
        HeapsortColumn.heapsortDescending(stringVals, stringIndex, 4, range.getRange());

        System.out.println("Original  (D): " + Arrays.toString(stringVals));
        System.out.println("Reordered (D): " + Arrays.toString(reorder(stringVals, stringIndex)));
        System.out.println("Index     (D): " + Arrays.toString(stringIndex));
    }

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
    public void booleanSorting() {
        int values[] = new int[] {0, ColumnView.NA, 1, 1, 0, ColumnView.NA };
        int[] indexes = new int[] { 0, 1, 2, 3, 4, 5 };
        BooleanColumnView columnView = new BooleanColumnView(values);

        columnView.order(indexes, SortModel.Dir.ASC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(ColumnView.NA, ColumnView.NA, 0, 0, 1, 1));

        columnView.order(indexes, SortModel.Dir.DESC, null);
        assertThat(reorder(values, indexes), isArrayEqualTo(1, 1, 0, 0, ColumnView.NA, ColumnView.NA));
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

    private int[] reorder(int[] original, int[] sortVector) {
        int[] output = new int[original.length];
        for (int i=0; i<output.length; i++) {
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

    private double[] reorder(double[] original, int[] sortVector) {
        double[] output = new double[original.length];
        for (int i=0; i<output.length; i++) {
            output[i] = original[sortVector[i]];
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
}