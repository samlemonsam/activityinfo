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
