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
package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.HTML;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

import java.util.Collection;

public class FieldView {
    private final ResourceId fieldId;
    private final FieldWidget widget;
    private HTML validationMessage;

    public FieldView(ResourceId fieldId, FieldWidget widget, HTML validationMessage) {
        this.fieldId = fieldId;
        this.widget = widget;
        this.validationMessage = validationMessage;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public FieldWidget getWidget() {
        return widget;
    }

    public void updateView(FormInputViewModel viewModel) {
        widget.setRelevant(viewModel.isRelevant(fieldId));

        if(viewModel.isMissing(fieldId)) {
            validationMessage.setText(I18N.CONSTANTS.requiredFieldMessage());
            validationMessage.setVisible(true);
        } else {
            Collection<String> validationErrors = viewModel.getValidationErrors(fieldId);
            if(!validationErrors.isEmpty()) {
                validationMessage.setText(validationErrors.iterator().next());
                validationMessage.setVisible(true);
            } else {
                validationMessage.setVisible(false);
            }
        }
    }

    public void init(FormInputViewModel viewModel) {
        FieldValue fieldValue = viewModel.getField(fieldId);
        if(fieldValue == null) {
            widget.clear();
        } else {
            widget.init(fieldValue);
        }
    }
}

