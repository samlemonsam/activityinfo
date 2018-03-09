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
package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;
import org.activityinfo.io.xform.form.Input;
import org.activityinfo.model.type.number.QuantityType;

class QuantityFieldBuilder implements OdkFormFieldBuilder {
    final private String units;

    QuantityFieldBuilder(QuantityType quantityType) {
        this.units = quantityType.getUnits();
    }

    @Override
    public BindingType getModelBindType() {
        return BindingType.DECIMAL;
    }

    @Override
    public Optional<String> getConstraint() {
        return Optional.absent();
    }

    @Override
    public BodyElement createBodyElement(String ref, String label, String hint) {
        Input input = new Input();
        input.setRef(ref);

        if (units == null) {
            input.setLabel(label);
        } else if (label == null) {
            input.setLabel(units);
        } else {
            input.setLabel(label + " [" + units + ']');
        }
        input.setHint(hint);

        return input;
    }
}
