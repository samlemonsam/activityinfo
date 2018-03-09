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
package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.Arrays;
import java.util.Random;

public class DiscreteTextGenerator implements Supplier<FieldValue> {

    public static final String[] NAMES = new String[] { "Bob", "George", "Joe", "Melanie", "Sue", "Franz", "Jane", "Matilda" };


    private final String[] values;
    private final Random random;
    private double probabilityMissing;

    public DiscreteTextGenerator(double probabilityMissing, String... values) {
        this.probabilityMissing = probabilityMissing;
        this.values = Arrays.copyOf(values, values.length);
        this.random = new Random(356432L);
    }

    @Override
    public FieldValue get() {

        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        }

        int valueIndex = random.nextInt(values.length);
        String value = values[valueIndex];
        return TextValue.valueOf(value);
    }
}
