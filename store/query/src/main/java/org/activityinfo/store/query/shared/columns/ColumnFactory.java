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
package org.activityinfo.store.query.shared.columns;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.PendingSlot;

/**
 * Creates optimizing column builder.
 *
 * <p>We use different implementations on the server and on the client
 * that use different types of optimizations.</p>
 */
public interface ColumnFactory {

    CursorObserver<FieldValue> newStringBuilder(PendingSlot<ColumnView> result, StringReader reader);

    CursorObserver<FieldValue> newDoubleBuilder(PendingSlot<ColumnView> result, DoubleReader reader);

    CursorObserver<FieldValue> newEnumBuilder(PendingSlot<ColumnView> result, EnumType enumType);

    CursorObserver<FieldValue> newForeignKeyBuilder(ResourceId rightFormId, PendingSlot<ForeignKey> value);

    PrimaryKeyMap newPrimaryKeyMap(ColumnView columnView);
}
