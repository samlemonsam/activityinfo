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
package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.Cursor;
import org.activityinfo.store.spi.CursorObserver;

/**
 * Column query on a "normal" sql table, where fields are mapped to
 * a fixed column in the table 
 */
public class SimpleTableColumnQueryBuilder implements ColumnQueryBuilder {

    private final MySqlCursorBuilder cursorBuilder;

    public SimpleTableColumnQueryBuilder(MySqlCursorBuilder cursorBuilder) {
        this.cursorBuilder = cursorBuilder;
    }

    @Override
    public void only(ResourceId resourceId) {
        cursorBuilder.only(resourceId);
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        cursorBuilder.addResourceId(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        cursorBuilder.addField(fieldId, observer);
    }

    @Override
    public void execute() {
        Cursor open = cursorBuilder.open();
        while(open.next()) {
        }
    }
}
