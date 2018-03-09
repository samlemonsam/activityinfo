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
package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolder;

import javax.annotation.Nonnull;

/**
 * @author yuriyz on 01/15/2015.
 */
public class ContainerPropertiesPresenter {

    private final FormDesigner formDesigner;
    private final ContainerPropertiesPanel view;

    private HandlerRegistration labelKeyUpHandler;

    public ContainerPropertiesPresenter(@Nonnull FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        this.view = formDesigner.getFormDesignerPanel().getContainerPropertiesPanel();
    }

    public void show(@Nonnull final FieldsHolder fieldsHolder) {
        if (fieldsHolder.equals(formDesigner.getModel().getSelectedWidgetContainer())) {
            return; // skip, container is already selected
        }

        reset();

        formDesigner.getFormDesignerPanel().setContainerPropertiesPanelVisible();

        view.getLabelGroup().setVisible(true);
        view.getLabel().setValue(Strings.nullToEmpty(fieldsHolder.getElementContainer().getLabel()));

        validateLabel();

        labelKeyUpHandler = view.getLabel().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (validateLabel()) {
                    fieldsHolder.setLabel(view.getLabel().getValue());
                    fieldsHolder.updateUi();
                }
            }
        });
    }
    
    public void reset() {
        if (labelKeyUpHandler != null) {
            labelKeyUpHandler.removeHandler();
        }
        view.getLabel().setValue("");
    }


    /**
     * Returns whether code is valid.
     *
     * @return whether code is valid
     */
    private boolean validateLabel() {
        view.getLabelGroup().setShowValidationMessage(false);
        if (Strings.isNullOrEmpty(view.getLabel().getValue())) {
            view.getLabelGroup().setShowValidationMessage(true);
            return false;
        }
        return true;
    }
}

