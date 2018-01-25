package org.activityinfo.promise;

import com.google.common.base.Function;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PromiseMapTest {

    private List<Integer> input;
    private List<Promise<String>> output;
    private Promise<List<String>> map;

    @Before
    public void setup() {

        input = Arrays.asList(0, 1);

        output = new ArrayList<>();
        output.add(new Promise<String>());
        output.add(new Promise<String>());

        map = Promise.map(input, new Function<Integer, Promise<String>>() {
            @Override
            public Promise<String> apply(Integer integer) {
                return output.get(integer);
            }
        });
    }

    @Test
    public void inOrder() {

        assertFalse(map.isSettled());

        output.get(0).onSuccess("0");

        assertFalse(map.isSettled());

        output.get(1).onSuccess("1");

        assertTrue(map.isSettled());
        assertThat(map.get(), Matchers.contains("0", "1"));
    }


}
