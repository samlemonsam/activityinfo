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
import org.activityinfo.io.xform.form.Input;

class SimpleInputBuilder implements OdkFormFieldBuilder {
    final private BindingType modelBindType;
    private Optional<String> constraint;

    SimpleInputBuilder(BindingType modelBindType, Optional<String> constraint) {
        this.modelBindType = modelBindType;
        this.constraint = constraint;
    }

    SimpleInputBuilder(BindingType modelBindType) {
        this(modelBindType, Optional.<String>absent());
    }

    @Override
    public BindingType getModelBindType() {
        return modelBindType;
    }

    @Override
    public Optional<String> getConstraint() {
        return constraint;
    }

    @Override
    public Input createBodyElement(String ref, String label, String hint) {
        Input input = new Input();

        input.setRef(ref);
        input.setLabel(label);
        input.setHint(hint);

        return input;
    }
}
