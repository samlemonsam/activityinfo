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
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.Random;

/**
 * Generates values for a single select field
 */
public class EnumGenerator implements Supplier<FieldValue> {

    private EnumType enumType;
    private double probabilityMissing;
    private Random random;

    public EnumGenerator(FormField field, int seed) {
        this.enumType = (EnumType) field.getType();
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.25;
        this.random = new Random(seed);
    }

    public EnumGenerator(FormField field) {
        this(field, field.getId().hashCode());
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        }

        int itemIndex = random.nextInt(enumType.getValues().size());
        EnumItem item = enumType.getValues().get(itemIndex);

        return new EnumValue(item.getId());
    }
}
