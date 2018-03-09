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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

import java.util.Random;

public class IntegerGenerator implements Supplier<FieldValue> {
    private int minValue;
    private int range;
    private final Random random;
    private double probabilityMissing;

    public IntegerGenerator(int min, int max, double probabilityMissing) {
        Preconditions.checkArgument(max > min, "max must be greater than min");
        this.minValue = min;
        this.range = (max - min);
        this.random = new Random(432222L);
        this.probabilityMissing = probabilityMissing;
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        } else {
            double quantity = minValue + random.nextInt(range);
            return new Quantity(quantity);
        }
    }
}
