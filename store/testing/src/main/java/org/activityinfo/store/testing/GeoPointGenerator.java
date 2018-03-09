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
import org.activityinfo.model.type.geo.GeoPoint;

import java.util.Random;


public class GeoPointGenerator implements Supplier<FieldValue> {


    private final Random random = new Random(235564434L);
    private double probabilityMissing;


    public GeoPointGenerator(FormField field) {
        if(field.isRequired()) {
            probabilityMissing = 0;
        } else {
            probabilityMissing = 0.20;
        }
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        } else {
            double latitude = random.nextDouble() * 180d - 90d;
            double longitude = random.nextDouble() * 360d - 180d;
            return new GeoPoint(latitude, longitude);
        }
    }
}
