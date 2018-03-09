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
package org.activityinfo.ui.client.store;

import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

import java.util.HashSet;
import java.util.Set;

public interface FormChange {

    boolean isFormChanged(ResourceId formId);

    boolean isSchemaChanged(ResourceId formId);

    boolean isRecordChanged(RecordRef recordRef);

    static FormChange from(RecordTransaction tx) {

        Set<ResourceId> updatedForms = new HashSet<>();

        for (RecordUpdate recordUpdate : tx.getChanges()) {
            updatedForms.add(recordUpdate.getFormId());
        }

        return new FormChange() {
            @Override
            public boolean isFormChanged(ResourceId formId) {
                return updatedForms.contains(formId);
            }

            @Override
            public boolean isSchemaChanged(ResourceId formId) {
                return false;
            }

            @Override
            public boolean isRecordChanged(RecordRef recordRef) {
                return updatedForms.contains(recordRef.getFormId());
            }
        };
    }


}
