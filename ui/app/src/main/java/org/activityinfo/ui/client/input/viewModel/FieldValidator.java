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
package org.activityinfo.ui.client.input.viewModel;

import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.util.Optional;

public class FieldValidator {
    private ResourceId fieldId;
    private FieldValueValidator validator;

    public FieldValidator(ResourceId fieldId, FieldValueValidator validator) {
        this.fieldId = fieldId;
        this.validator = validator;
    }

    public void run(FormInstance record, Multimap<ResourceId, String> validationErrors) {
        FieldValue value = record.get(fieldId);
        if(value != null) {
            Optional<String> errorMessage = validator.validate(value);
            if(errorMessage.isPresent()) {
                validationErrors.put(fieldId, errorMessage.get());
            }
        }
    }
}
