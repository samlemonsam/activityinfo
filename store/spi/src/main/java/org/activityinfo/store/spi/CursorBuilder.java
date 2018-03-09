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
package org.activityinfo.store.spi;

import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

/**
 * Contract for obtaining a {@link Cursor}
 * to iterate over a the Resources in a Collection.
 *
 * <p>Callers must explicitly specify which fields to retrieve by calling
 * {@code addField()}
 */
public interface CursorBuilder {


    void addResourceId(CursorObserver<ResourceId> observer);

    void addField(FormulaNode node, CursorObserver<FieldValue> observer);

    void addField(FieldPath fieldPath, CursorObserver<FieldValue> observer);

    /**
     * Adds a field to the cursor
     * @param fieldId the id of the field to add to the ResourceId
     * @param observer an observer to receive the id of each {@code Reso}
     */
    void addField(ResourceId fieldId, CursorObserver<FieldValue> observer);


    /**
     *
     * Opens the cursor at the beginning of the {@code Collection}.
     *
     * @return an open Cursor
     */
    Cursor open();



}
