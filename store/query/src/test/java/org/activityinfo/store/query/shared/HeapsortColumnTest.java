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

import org.activityinfo.model.query.SortModel;
import org.activityinfo.model.util.HeapsortColumn;
import org.junit.Test;

import java.util.Arrays;

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
    public void doubleColumnSortedByValueAsc() {
        int id[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        double values[] = { 50, 150, 90, 3, 9, 10, 30, 4, 5, 6 };

        System.out.println(Arrays.toString(values));
        System.out.println(Arrays.toString(id));

        SortModel.Range range = new SortModel.Range(0,9);
        HeapsortColumn.heapsortAscending(values, id, range.getRangeSize(), range.getRange());

        System.out.println("Original  (A): " + Arrays.toString(values));
        System.out.println("Reordered (A): " + Arrays.toString(reorder(values, id)));
        System.out.println("Index     (A): " + Arrays.toString(id));

    }

    @Test
    public void doubleColumnSortedByValueDsc() {
        int id[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        double values[] = { 50, 150, 90, 3, 9, 10, 30, 4, 5, 6 };

        System.out.println(Arrays.toString(values));
        System.out.println(Arrays.toString(id));

        SortModel.Range range = new SortModel.Range(0,9);
        HeapsortColumn.heapsortDescending(values, id, range.getRangeSize(), range.getRange());

        System.out.println("Original  (D): " + Arrays.toString(values));
        System.out.println("Reordered (D): " + Arrays.toString(reorder(values, id)));
        System.out.println("Index     (D): " + Arrays.toString(id));

    }

    @Test
    public void multipleSort() {
        int masterRowId[] = {0, 1, 2, 3, 4, 5, 6};
        int sortVector[] = masterRowId.clone();
        String stringCol[] = {"C", "D", "A", "E", "B", "C", "C"};
        double doubleCol[] = {15, 17, 12, 13, 12, 12, 11};

        SortModel.Range range = new SortModel.Range(0,6);

        System.out.println("Original Ids: " + Arrays.toString(masterRowId));
        System.out.println("Sort Vector:  " + Arrays.toString(sortVector));
        System.out.println("String Col:   " + Arrays.toString(stringCol));
        System.out.println("Double Col:   " + Arrays.toString(doubleCol));

        HeapsortColumn.heapsortAscending(stringCol, sortVector, range.getRangeSize(), range.getRange());

        System.out.println("1st Sort on String Col");
        System.out.println("Original Ids:             " + Arrays.toString(masterRowId));
        System.out.println("Sort Vector:              " + Arrays.toString(sortVector));
        System.out.println("String Col:               " + Arrays.toString(stringCol));
        System.out.println("String Col (Reordered):   " + Arrays.toString(reorder(stringCol, sortVector)));
        System.out.println("Double Col:               " + Arrays.toString(doubleCol));
        System.out.println("Double Col (Reordered):   " + Arrays.toString(reorder(doubleCol, sortVector)));

        range = new SortModel.Range(2,4);

        HeapsortColumn.heapsortAscending(doubleCol, sortVector, range.getRangeSize(), range.getRange());

        System.out.println("2nd Sort on Double Col");
        System.out.println("Original Ids:             " + Arrays.toString(masterRowId));
        System.out.println("Sort Vector:              " + Arrays.toString(sortVector));
        System.out.println("String Col:               " + Arrays.toString(stringCol));
        System.out.println("String Col (Reordered):   " + Arrays.toString(reorder(stringCol, sortVector)));
        System.out.println("Double Col:               " + Arrays.toString(doubleCol));
        System.out.println("Double Col (Reordered):   " + Arrays.toString(reorder(doubleCol, sortVector)));

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


}