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
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MultiEnumGenerator implements Supplier<FieldValue> {

    private EnumType enumType;
    private double probabilityMissing;
    private final double[] probabilities;
    private Random random;

    public MultiEnumGenerator(FormField field, double... probabilities) {
        this.enumType = (EnumType) field.getType();
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.10;
        this.probabilities = Arrays.copyOf(probabilities, probabilities.length);
        this.random = new Random(field.getId().hashCode());
        assert probabilities.length == enumType.getValues().size();
    }

    public MultiEnumGenerator(FormField field) {
        this.enumType = (EnumType) field.getType();
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.10;
        this.probabilities = new double[enumType.getValues().size()];
        Arrays.fill(probabilities, 0.15);

        this.random = new Random(field.getId().hashCode());
        assert probabilities.length == enumType.getValues().size();
    }

    @Override
    public FieldValue get() {
        if(random.nextDouble() < probabilityMissing) {
            return null;
        }
        List<ResourceId> ids = new ArrayList<>();
        for (int i = 0; i < probabilities.length; i++) {
            if(random.nextDouble() < probabilities[i]) {
                ids.add(enumType.getValues().get(i).getId());
            }
        }
        return new EnumValue(ids);
    }
}
