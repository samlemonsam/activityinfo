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
import org.activityinfo.io.xform.form.*;
import org.activityinfo.model.type.Cardinality;

import java.util.List;

class SelectBuilder implements OdkFormFieldBuilder {
    final private BindingType modelBindType;
    final private Cardinality cardinality;
    final private List<Item> items;

    SelectBuilder(BindingType modelBindType, SelectOptions selectOptions) {
        this.modelBindType = modelBindType;
        this.cardinality = selectOptions.getCardinality();
        this.items = selectOptions.getItems();
    }

    @Override
    public BindingType getModelBindType() {
        return modelBindType;
    }

    @Override
    public Optional<String> getConstraint() {
        return Optional.absent();
    }

    @Override
    public SelectElement createBodyElement(String ref, String label, String hint) {
        SelectElement select;
        switch(cardinality) {
            case SINGLE:
                select = new Select1();
                break;
            case MULTIPLE:
                select = new Select();
                break;
            default:
                throw new IllegalStateException("Cardinality: " + cardinality);
        }

        select.setRef(ref);
        select.setLabel(label);
        select.getItems().addAll(items);
        select.setHint(hint);
        return select;
    }
}
