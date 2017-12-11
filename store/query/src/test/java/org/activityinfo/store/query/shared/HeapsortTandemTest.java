package org.activityinfo.store.query.shared;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HeapsortTandemTest {

    @Test
    public void sortedAsc() {
        
        int id[] = {-1, -1, -1, -1, -1, -1, -1, 0, 0, 0 };
        double values[] = { 50, 150, 90, 3, 9, 10, 30, 4, 5, 6 };


        System.out.println(Arrays.toString(id));
        System.out.println(Arrays.toString(values));

        HeapsortTandem.heapsortAscending(id, values, 10);

        System.out.println("Sorted (A): " + Arrays.toString(id));
        System.out.println("Tandem (A): " + Arrays.toString(values));

        assertThat( values[7] + values[8] + values[9], equalTo(15d));
    }

    @Test
    public void sortedDsc() {

        int id[] = {-1, -1, -1, -1, -1, -1, -1, 0, 0, 0 };
        double values[] = { 50, 150, 90, 3, 9, 10, 30, 4, 5, 6 };


        System.out.println(Arrays.toString(id));
        System.out.println(Arrays.toString(values));

        HeapsortTandem.heapsortDescending(id, values, 10);

        System.out.println("Sorted (D): " + Arrays.toString(id));
        System.out.println("Tandem (D): " + Arrays.toString(values));

        assertThat( values[0] + values[1] + values[2], equalTo(15d));
    }

    @Test
    public void stringSortedByIndexAsc() {

        String stringVals[] = {"c", "a", "b", "s"};
        int stringIndex[] = {2, 3, 1, 4};

        System.out.println(Arrays.toString(stringIndex));
        System.out.println(Arrays.toString(stringVals));

        HeapsortTandem.heapsortAscending(stringIndex, stringVals, 4);

        System.out.println("Sorted (A): " + Arrays.toString(stringIndex));
        System.out.println("Tandem (A): " + Arrays.toString(stringVals));

    }

    @Test
    public void stringSortedByIndexDsc() {

        String stringVals[] = {"c", "a", "b", "s"};
        int stringIndex[] = {2, 3, 1, 4};

        System.out.println(Arrays.toString(stringIndex));
        System.out.println(Arrays.toString(stringVals));

        HeapsortTandem.heapsortDescending(stringIndex, stringVals, 4);

        System.out.println("Sorted (D): " + Arrays.toString(stringIndex));
        System.out.println("Tandem (D): " + Arrays.toString(stringVals));

    }

    @Test
    public void doubleSortedByIndexAsc() {
        int id[] = {5, 3, 2, 6, 1, 4, 7, 10, 9, 8 };
        double values[] = { 50, 150, 90, 3, 9, 10, 30, 4, 5, 6 };

        System.out.println(Arrays.toString(id));
        System.out.println(Arrays.toString(values));

        HeapsortTandem.heapsortAscending(id, values, 10);

        System.out.println("Sorted (A): " + Arrays.toString(id));
        System.out.println("Tandem (A): " + Arrays.toString(values));

    }

    @Test
    public void doubleSortedByIndexDsc() {
        int id[] = {5, 3, 2, 6, 1, 4, 7, 10, 9, 8 };
        double values[] = { 50, 150, 90, 3, 9, 10, 30, 4, 5, 6 };

        System.out.println(Arrays.toString(id));
        System.out.println(Arrays.toString(values));

        HeapsortTandem.heapsortDescending(id, values, 10);

        System.out.println("Sorted (D): " + Arrays.toString(id));
        System.out.println("Tandem (D): " + Arrays.toString(values));

    }

}