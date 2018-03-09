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

import com.google.inject.ImplementedBy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;

@ImplementedBy(ResourceLocatorSyncImpl.class)
public interface ResourceLocatorSync extends FormClassProvider {
    
    void persist(FormInstance formInstance);

    FormClass getFormClass(ResourceId formId);

    FormTree getFormTree(ResourceId formId);

    List<ReferenceChoice> getReferenceChoices(Collection<ResourceId> range);

    void persist(FormClass formClass);

    ColumnSet query(QueryModel model);
}
