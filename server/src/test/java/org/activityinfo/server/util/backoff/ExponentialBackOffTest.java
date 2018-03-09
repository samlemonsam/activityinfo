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
package org.activityinfo.server.util.backoff;

import org.activityinfo.legacy.shared.util.ExponentialBackOff;
import org.activityinfo.legacy.shared.util.NanoClock;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author yuriyz on 10/16/2014.
 */
public class ExponentialBackOffTest {

    @Test
    public void test() throws IOException {
        NanoClock nanoClock = new NanoClock() {
            @Override
            public long nanoTime() {
                return System.nanoTime();
            }
        };

        ExponentialBackOff defaultBackOff = new ExponentialBackOff.Builder().setNanoClock(nanoClock).build();

        ExponentialBackOff backOff = new ExponentialBackOff.Builder()
                .setInitialIntervalMillis(TimeUnit.SECONDS.toMillis(10))
                .setMultiplier(2) // increase in 3 times
                .setNanoClock(nanoClock)
                .build();
        for (int i = 0; i < 10; i++) {
            System.out.println(defaultBackOff.nextBackOffMillis() + " " + backOff.nextBackOffMillis());
        }
    }
}
