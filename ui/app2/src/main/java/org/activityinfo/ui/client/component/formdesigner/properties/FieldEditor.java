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
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;
import org.activityinfo.ui.client.component.formdesigner.skip.RelevanceDialog;
import org.activityinfo.ui.client.widget.CheckBox;
import org.activityinfo.ui.client.widget.TextArea;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.form.FormGroup;

import java.util.List;

/**
 * @author yuriyz on 7/9/14.
 */
public class FieldEditor implements IsWidget {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);
    private FormField formField;
    private FieldWidgetContainer fieldWidgetContainer;

    interface OurUiBinder extends UiBinder<Widget, FieldEditor> {
    }

    private final Widget widget;

    @UiField
    TextBox label;
    @UiField
    HTMLPanel panel;
    @UiField
    TextArea description;
    @UiField
    CheckBox required;
    @UiField
    HTMLPanel requiredGroup;
    @UiField
    Button relevanceButton;
    @UiField
    HTMLPanel relevanceGroup;
    @UiField
    SpanElement relevanceExpression;
    @UiField
    CheckBox visible;
    @UiField
    FormGroup visibleGroup;
    @UiField
    CheckBox key;
    @UiField
    FormGroup keyGroup;
    @UiField
    RadioButton relevanceEnabled;
    @UiField
    RadioButton relevanceEnabledIf;
    @UiField
    TextBox code;
    @UiField
    FormGroup codeGroup;
    @UiField
    FormGroup labelGroup;
    @UiField
    FormGroup descriptionGroup;
    @UiField
    QuantityTypeEditor quantityTypeEditor;
    @UiField
    CalculatedTypeEditor calculatedTypeEditor;
    @UiField
    TextTypeEditor textTypeEditor;
    @UiField
    SerialNumberTypeEditor serialNumberTypeEditor;
    @UiField
    EnumTypeEditor enumTypeEditor;



    private FormDesigner formDesigner;

    public FieldEditor() {
        this.widget = uiBinder.createAndBindUi(this);
    }

    public void start(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        formDesigner.getEventBus().addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                WidgetContainer widgetContainer = event.getSelectedItem();
                if (widgetContainer instanceof FieldWidgetContainer) {
                    show((FieldWidgetContainer) widgetContainer);
                }
            }
        });
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    public int getOffsetHeight() {
        return widget.getOffsetHeight();
    }

    public void setVisible(boolean visible) {
        widget.setVisible(visible);
    }

    public FieldEditor getView() {
        return this;
    }


    private void show(final FieldWidgetContainer fieldWidgetContainer) {
        this.fieldWidgetContainer = fieldWidgetContainer;
        formField = fieldWidgetContainer.getFormField();

        formDesigner.getFormDesignerPanel().setPropertiesPanelVisible();

        boolean isPartner = FormDesigner.isPartner(formField);

        label.setValue(Strings.nullToEmpty(formField.getLabel()));
        description.setValue(Strings.nullToEmpty(formField.getDescription()));
        required.setValue(formField.isRequired());
        visible.setValue(formField.isVisible());
        key.setValue(formField.isKey());
        code.setValue(Strings.nullToEmpty(formField.getCode()));

        required.setEnabled(!isPartner);
        visible.setEnabled(!isPartner);
        relevanceGroup.setVisible(!isPartner);
        keyGroup.setVisible(isElligbleToBeKey(formField));

        setRelevanceState(formField, true);
        validateCode(fieldWidgetContainer);
        validateLabel();

        quantityTypeEditor.show(fieldWidgetContainer);
        calculatedTypeEditor.show(fieldWidgetContainer);
        textTypeEditor.show(fieldWidgetContainer);
        serialNumberTypeEditor.show(fieldWidgetContainer);
        enumTypeEditor.show(fieldWidgetContainer);
    }

    private boolean isElligbleToBeKey(FormField formField) {
        FieldType type = formField.getType();
        if(type instanceof TextType) {
            return true;
        }
        if(type instanceof ReferenceType) {
            ReferenceType referenceType = (ReferenceType) type;
            if(referenceType.getCardinality() == Cardinality.SINGLE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether code is valid.
     *
     * @return whether code is valid
     */
    private boolean validateCode(FieldWidgetContainer fieldWidgetContainer) {
        codeGroup.setShowValidationMessage(false);
        String code = this.code.getValue();
        if (Strings.isNullOrEmpty(code)) {
            return true;
        }

        if (!FormField.isValidCode(code)) {
            codeGroup.showValidationMessage(I18N.CONSTANTS.invalidCodeMessage());
            return false;
        } else {

            // check whether code is unique
            List<FormField> formFields = fieldWidgetContainer.getFormDesigner().getModel().getAllFormsFields();
            formFields.remove(fieldWidgetContainer.getFormField());

            for (FormField formField : formFields) {
                if (code.equals(formField.getCode())) {
                    codeGroup.showValidationMessage(I18N.CONSTANTS.duplicateCodeMessage());
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Returns whether code is valid.
     *
     * @return whether code is valid
     */
    private boolean validateLabel() {
        labelGroup.setShowValidationMessage(false);
        if (Strings.isNullOrEmpty(label.getValue())) {
            labelGroup.setShowValidationMessage(true);
            return false;
        }
        return true;
    }


    @UiHandler("label")
    void onLabelChanged(KeyUpEvent event) {
        if(validateLabel()) {
            formField.setLabel(label.getValue());
            fireUpdate();
        }
    }

    @UiHandler("description")
    void onDescriptionChanged(KeyUpEvent event) {
        formField.setDescription(description.getValue());
        fireUpdate();
    }

    @UiHandler("code")
    void onCodeChanged(KeyUpEvent event) {
        formField.setCode(code.getValue());
        fireUpdate();
    }

    @UiHandler("required")
    void onRequiredChange(ValueChangeEvent<Boolean> event) {
        formField.setRequired(event.getValue());
        fireUpdate();
    }

    @UiHandler("visible")
    void onVisibleChanged(ValueChangeEvent<Boolean> event) {
        formField.setVisible(event.getValue());
        fireUpdate();
    }


    @UiHandler("key")
    void onKeyChanged(ValueChangeEvent<Boolean> event) {
        formField.setKey(event.getValue());
        fireUpdate();
    }


    @UiHandler("relevanceButton")
    void onRelevanceClicked(ClickEvent event) {
        RelevanceDialog dialog = new RelevanceDialog(fieldWidgetContainer, FieldEditor.this);
        dialog.show();
    }

    @UiHandler("relevanceEnabled")
    void onRelevanceEnabled(ValueChangeEvent<Boolean> event) {
        formField.setRelevanceConditionExpression(null);
        setRelevanceState(formField, false);
    }

    @UiHandler("relevanceEnabledIf")
    void onRelevanceEnabledIf(ValueChangeEvent<Boolean> event) {
        setRelevanceState(formField, false);
    }

    private void fireUpdate() {
        fieldWidgetContainer.syncWithModel();
    }

    public void setRelevanceState(FormField formField, boolean setRadioButtons) {
        if(setRadioButtons) {
            if (formField.hasRelevanceConditionExpression()) {
                relevanceEnabledIf.setValue(true);
            } else {
                relevanceEnabled.setValue(true);
            }
        }
        relevanceButton.setEnabled(relevanceEnabledIf.getValue());

//        view.getRelevanceState().setText(formField.hasRelevanceConditionExpression() ? I18N.CONSTANTS.defined() : I18N.CONSTANTS.no());
//        view.getRelevanceExpression().setInnerText(formField.getRelevanceConditionExpression());
//        if (formField.hasRelevanceConditionExpression()) {
//            view.getRelevanceExpression().removeClassName("hide");
//        } else if (!view.getRelevanceExpression().getClassName().contains("hide")) {
//            view.getRelevanceExpression().addClassName("hide");
//        }
    }

}
