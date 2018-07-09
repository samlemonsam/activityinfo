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

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.ui.client.input.view.InputResources;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

import java.util.Collection;

public class FieldView implements IsWidget {
    private final ResourceId fieldId;
    private final FieldWidget widget;
    private HTML validationMessage;
    private final CssFloatLayoutContainer container;

    private boolean valid = true;
    private boolean visible = true;

    public FieldView(FormField field, FieldWidget fieldWidget, int horizontalPadding) {

        this.fieldId = field.getId();
        this.widget = fieldWidget;

        Label fieldLabel = new Label(field.getLabel() + requiredMarker(field));
        fieldLabel.addStyleName(InputResources.INSTANCE.style().fieldLabel());

        validationMessage = new HTML();
        validationMessage.addStyleName(InputResources.INSTANCE.style().validationMessage());
        validationMessage.setVisible(false);

        container = new CssFloatLayoutContainer();
        container.setStyleName(InputResources.INSTANCE.style().field());
        container.add(fieldLabel, new CssFloatLayoutContainer.CssFloatData(1));
        container.add(fieldWidget, new CssFloatLayoutContainer.CssFloatData(1,
                new Margins(5, horizontalPadding, 5, horizontalPadding)));

        if (!Strings.isNullOrEmpty(field.getDescription())) {
            Label descriptionLabel = new Label(field.getDescription());
            descriptionLabel.addStyleName(InputResources.INSTANCE.style().fieldDescription());
            container.add(descriptionLabel,
                    new CssFloatLayoutContainer.CssFloatData(1));
        }

        container.add(validationMessage, new CssFloatLayoutContainer.CssFloatData(1));
    }

    private String requiredMarker(FormField field) {
        if(field.isRequired()) {
            return " *";
        } else {
            return "";
        }
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public FieldWidget getWidget() {
        return widget;
    }

    public boolean updateView(FormInputViewModel viewModel) {

        boolean requiresLayout = false;

        widget.setRelevant(viewModel.isRelevant(fieldId) && !viewModel.isLocked());

        if(viewModel.isMissingErrorVisible(fieldId)) {
            invalidate(I18N.CONSTANTS.requiredFieldMessage());
        } else {
            Collection<String> validationErrors = viewModel.getValidationErrors(fieldId);
            if(!validationErrors.isEmpty()) {
                invalidate(validationErrors.iterator().next());
            } else {
                valid = true;
                validationMessage.setVisible(false);
                container.removeStyleName(InputResources.INSTANCE.style().fieldInvalid());
            }
        }

        return requiresLayout;
    }

    public void invalidate(String message) {
        valid = false;
        validationMessage.setText(message);
        validationMessage.setVisible(true);
        container.addStyleName(InputResources.INSTANCE.style().fieldInvalid());
    }

    public void init(FormInputViewModel viewModel) {
        FieldValue fieldValue = viewModel.getField(fieldId);
        if(fieldValue == null) {
            widget.clear();
        } else {
            widget.init(fieldValue);
        }
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * Scrolls this field into view and focus on the first input within the field.
     */
    public void focusTo() {
        container.getElement().scrollIntoView();
        widget.focus();
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}

