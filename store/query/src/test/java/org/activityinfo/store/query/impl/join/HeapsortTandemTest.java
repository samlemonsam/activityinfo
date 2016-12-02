package org.activityinfo.store.query.impl.join;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class HeapsortTandemTest {

    @Test
    public void sorted() {
        
        int masterRowId[] = {-1, -1, -1, -1, -1, -1, -1, 0, 0, 0 };
        double values[] = { 50, 150, 90, 3, 9, 10, 30, 4, 5, 6 };


        System.out.println(Arrays.toString(masterRowId));
        System.out.println(Arrays.toString(values));
        
        HeapsortTandem.heapsortDescending(masterRowId, values, 10);
        
        assertThat( values[7] + values[8] + values[9], equalTo(15d));
    }
    
}