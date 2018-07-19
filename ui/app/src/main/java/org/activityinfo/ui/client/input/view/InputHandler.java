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
package org.activityinfo.ui.client.input.view;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;

/**
 * Applies inputs from the view to the model.
 */
public interface InputHandler {

    void updateModel(RecordRef record, ResourceId fieldId, FieldInput value);

    void touchField(RecordRef recordRef, ResourceId fieldId);

    void addSubRecord(RecordRef subRecordRef);

    void deleteSubRecord(RecordRef recordRef);

    void changeActiveSubRecord(ResourceId fieldId, RecordRef newActiveRef);

    void updateSubModel(FormInputModel update);

    void validationRequested(RecordRef recordRef);
}

