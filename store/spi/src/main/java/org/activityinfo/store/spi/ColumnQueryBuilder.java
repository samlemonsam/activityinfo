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

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;


/**
 * Builds a query that returns the requested fields as column streams.
 */
public interface ColumnQueryBuilder {

    void only(ResourceId resourceId);
    
    /**
     * Adds the {@code resourceId} to the list of columns to fetch.
     * 
     * @param observer interface to the object that will receive the stream
     *                 of {@code resourceIds} when {@link #execute()} is called.                 
     */
    void addResourceId(CursorObserver<ResourceId> observer);

    /**
     * Adds the {@code fieldId} to the list of columns to fetch.
     * * 
     * @param fieldId the id of the column to fetch
     * @param observer interface to the object that will receive the stream
     *                 of column values when {@link #execute()} is called.                 
     */
    void addField(ResourceId fieldId, CursorObserver<FieldValue> observer);


    /**
     * Fetches the requested columns.
     */
    void execute();
}
