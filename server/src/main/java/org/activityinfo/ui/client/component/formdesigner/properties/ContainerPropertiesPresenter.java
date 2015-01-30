package org.activityinfo.ui.client.component.formdesigner.properties;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElementContainer;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.subform.SubFormKind;
import org.activityinfo.model.type.subform.SubFormKindRegistry;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolder;

/**
 * @author yuriyz on 01/15/2015.
 */
public class ContainerPropertiesPresenter {

    private final FormDesigner formDesigner;
    private final ContainerPropertiesPanel view;

    private HandlerRegistration labelKeyUpHandler;

    public ContainerPropertiesPresenter(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        this.view = formDesigner.getFormDesignerPanel().getContainerPropertiesPanel();
    }

    public void show(final FieldsHolder fieldsHolder) {
        reset();

        view.getLabel().setValue(Strings.nullToEmpty(fieldsHolder.getElementContainer().getLabel()));

        validateLabel();

        labelKeyUpHandler = view.getLabel().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (validateLabel()) {
                    fieldsHolder.getElementContainer().setLabel(view.getLabel().getValue());
                    fieldsHolder.updateUi();
                }
            }
        });

        if (isSubform(fieldsHolder)) {
            view.getSubformKindGroup().setVisible(true);
            view.getSubformKind().addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    String selectedValue = view.getSubformKind().getValue(view.getSubformKind().getSelectedIndex());
                    subformKindChanged(SubFormKindRegistry.get().getKind(selectedValue));
                }
            });
            FormClass subForm = (FormClass) fieldsHolder.getElementContainer();
            FormField subformOwnerField = formDesigner.getModel().getSubformOwnerField(subForm);
            // todo
        }
    }

    private boolean isSubform(FieldsHolder fieldsHolder) {
        FormElementContainer elementContainer = fieldsHolder.getElementContainer();
        return elementContainer instanceof FormClass && !elementContainer.equals(formDesigner.getRootFormClass());
    }

    private void subformKindChanged(SubFormKind newKind) {
        // todo
    }

    public void reset() {
        if (labelKeyUpHandler != null) {
            labelKeyUpHandler.removeHandler();
        }
        view.getSubformKindGroup().setVisible(false);
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
