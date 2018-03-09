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
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.Random;


public class QuantityGenerator implements Supplier<FieldValue> {

    private double minValue;
    private double maxValue;
    private double probabilityMissing;
    private final Random random;
    private String units;

    public QuantityGenerator(FormField field) {
        this.minValue = -15;
        this.maxValue = 100;
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.25;
        this.random = new Random(135L);
        this.units = ((QuantityType) field.getType()).getUnits();
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        } else {
            double quantity = minValue + (random.nextDouble() * (maxValue - minValue));
            return new Quantity(quantity);
        }
    }
}
