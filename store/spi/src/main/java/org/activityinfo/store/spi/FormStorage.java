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

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * Internal interface used to construct table queries.
 */
public interface FormStorage {

    /**
     * Fetches the permissions which apply to the given user for this collection.
     */
    FormPermissions getPermissions(int userId);
    
    /**
     * Retrieves a single resource from the ResourceStore.
     * @param resourceId the id of the resource to retrieve
     * @return the Resource
     */
    Optional<FormRecord> get(ResourceId resourceId);

    List<FormRecord> getSubRecords(ResourceId resourceId);



    /**
     * @return this collection's schema
     */
    FormClass getFormClass();

    
    
    void updateFormClass(FormClass formClass);
    
    /**
     * Adds a new resource to the collection. 
     * 
     * @param update the properties of the new resource
     * @throws java.lang.IllegalStateException if a resource with the given {@code resourceId} already exists              
     */
    void add(TypedRecordUpdate update);
    
    /**
     * Updates an existing resource within the collection
     * 
     * @param update the changes to apply to the resource
     * @throws java.lang.IllegalStateException if the resource does not exist, or if the update               
     */
    void update(TypedRecordUpdate update);

    

    ColumnQueryBuilder newColumnQuery();


    /**
     * 
     * @return the current version of this collection to lookup.
     */
    long cacheVersion();

    /**
     * Update the geometry associated with a specific record and field.
     */
    void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value);



}
