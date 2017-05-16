package org.activityinfo.ui.client.component.formdesigner.skip;
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

import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.model.expr.simple.SimpleCondition;
import org.activityinfo.model.expr.simple.SimpleOperator;
import org.activityinfo.model.expr.simple.SimpleOperators;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.ui.client.widget.Button;

import java.util.List;

/**
 * Allows the user to select a field, an operator (==, !=, etc), which together form
 * a {@link SimpleCondition}
 */
public class RelevanceRow implements IsWidget {


    private List<SimpleOperator> operators;

    interface OurUiBinder extends UiBinder<Widget, RelevanceRow> {
    }

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    private final Widget panel;

    private final List<FormField> fields;

    @UiField
    ListBox fieldListBox;

    @UiField
    ListBox operatorListBox;
    @UiField
    TextBox operandTextBox;
    @UiField
    DoubleBox operandDoubleBox;
    @UiField
    ListBox operandListBox;
    @UiField
    Button removeButton;

    private OperandEditor operandEditor;

    public RelevanceRow(List<FormField> fields, Optional<SimpleCondition> condition) {
        this.panel = uiBinder.createAndBindUi(this);
        this.fields = fields;

        for (FormField field : fields) {
            fieldListBox.addItem(field.getLabel());
        }


        if(condition.isPresent()) {
            init(condition.get());
        } else if(!fields.isEmpty()) {
            fieldListBox.setSelectedIndex(0);
            updateOperators(fields.get(0), null);
            updateOperandChoices(fields.get(0), null);
        } else {
        }
    }

    private void init(SimpleCondition simpleCondition) {
        int fieldIndex = indexOfField(simpleCondition.getFieldId());
        fieldListBox.setSelectedIndex(fieldIndex);

        FormField field = fields.get(fieldIndex);
        updateOperators(field, simpleCondition.getOperator());
        updateOperandChoices(field, simpleCondition.getValue());
    }

    private int indexOfField(ResourceId fieldId) {
        for (int i = 0; i < fields.size(); i++) {
            if(fields.get(i).getId().equals(fieldId)) {
                return i;
            }
        }
        throw new IllegalStateException("No such field " + fieldId);
    }

    private FormField getSelectedField() {
        if(fieldListBox.getSelectedIndex() < 0) {
            return null;
        } else {
            return fields.get(fieldListBox.getSelectedIndex());
        }
    }

    @UiHandler("fieldListBox")
    public void onFieldChanged(ChangeEvent event) {
        FormField field = getSelectedField();
        if(field != null) {
            updateOperators(field, getSelectedOperator());
            updateOperandChoices(field, getValue());
        }
    }

    private SimpleOperator getSelectedOperator() {
        int operatorIndex = operatorListBox.getSelectedIndex();
        if(operatorIndex == -1) {
            return null;
        }
        return operators.get(operatorIndex);
    }


    public void setRemoveEnabled(boolean enabled) {
        removeButton.setEnabled(enabled);
    }

    public HandlerRegistration addRemoveHandler(ClickHandler clickHandler) {
        return removeButton.addClickHandler(clickHandler);
    }

    /**
     * Updates the choice of operator when the selected field changes.
     */
    private void updateOperators(FormField field, SimpleOperator selectedOperator) {
        operators = SimpleOperators.forType(field.getType());
        operatorListBox.clear();
        for (SimpleOperator operator : operators) {
            operatorListBox.addItem(labelFor(operator));
        }

        int currentIndex = operators.indexOf(selectedOperator);
        if(currentIndex < 0) {
            operatorListBox.setSelectedIndex(0);
        } else {
            operatorListBox.setSelectedIndex(currentIndex);
        }
    }


    /**
     * Updates the operand interface based on the field type.
     *
     * @param field the chosen field
     * @param value the current operand choice.
     */
    private void updateOperandChoices(FormField field, FieldValue value) {
        if(operandEditor != null) {
            operandEditor.stop();
        }

        if(field.getType() instanceof TextType) {
            operandEditor = TEXT_OPERAND_EDITOR;

        } else if(field.getType() instanceof QuantityType) {
            operandEditor = QUANTITY_OPERAND_EDITOR;

        } else if(field.getType() instanceof EnumType) {
            operandEditor = ENUM_OPERAND_EDITOR;

        } else {
            throw new IllegalStateException();
        }

        operandEditor.init(field, value);
    }

    public FieldValue getValue() {
        if(operandEditor == null) {
            return null;
        }

        return operandEditor.getValue();
    }

    private String labelFor(SimpleOperator operator) {
        switch (operator) {
            case EQUALS:
                return "is";
            case NOT_EQUALS:
                return "is not";
            case GREATER_THAN:
                return "is greater than";
            case GREATER_THAN_EQUAL:
                return "is greater than or equal";
            case LESS_THAN:
                return "is less than";
            case LESS_THAN_EQUAL:
                return "is less than or equal to";
            case INCLUDES:
                return "includes";
            case NOT_INCLUDES:
                return "does not include";
            default:
                throw new IllegalArgumentException();
        }
    }


    @Override
    public Widget asWidget() {
        return panel;
    }


    public SimpleCondition buildCondition() {
        return new SimpleCondition(getSelectedField().getId(), getSelectedOperator(), getValue());
    }

    private interface OperandEditor {
        void init(FormField field, FieldValue value);
        FieldValue getValue();
        void stop();
    }

    private final OperandEditor TEXT_OPERAND_EDITOR = new OperandEditor() {

        @Override
        public void init(FormField field, FieldValue value) {
            operandTextBox.setVisible(true);

            if(value instanceof TextValue) {
                operandTextBox.setValue(((TextValue) value).asString());
            }
        }

        @Override
        public FieldValue getValue() {
            return TextValue.valueOf(operandTextBox.getValue());
        }

        @Override
        public void stop() {
            operandTextBox.setVisible(false);
        }
    };

    private final OperandEditor QUANTITY_OPERAND_EDITOR = new OperandEditor() {
        @Override
        public void init(FormField field, FieldValue value) {
            operandDoubleBox.setVisible(true);

            if(value instanceof Quantity) {
                operandDoubleBox.setValue(((Quantity) value).getValue());
            }
        }

        @Override
        public FieldValue getValue() {
            if(operandDoubleBox.getValue() == null) {
                return null;
            } else {
                return new Quantity(operandDoubleBox.getValue());
            }
        }

        @Override
        public void stop() {
            operandDoubleBox.setVisible(false);
        }
    };

    private final OperandEditor ENUM_OPERAND_EDITOR = new OperandEditor() {
        private List<EnumItem> enumItems;

        @Override
        public void init(FormField field, FieldValue value) {
            operandListBox.setVisible(true);

            EnumType type = (EnumType) field.getType();
            this.enumItems = type.getValues();

            operandListBox.clear();
            for (EnumItem enumItem : enumItems) {
                operandListBox.addItem(enumItem.getLabel());
            }

            if(value instanceof EnumValue) {
                selectEnumItem(((EnumValue) value));
            }
        }

        private void selectEnumItem(EnumValue value) {
            for (int i = 0; i < enumItems.size(); i++) {
                if (enumItems.get(i).getId().equals(value.getValueId())) {
                    operandListBox.setSelectedIndex(i);
                    return;
                }
            }
        }

        @Override
        public FieldValue getValue() {
            int selectedItem = operandListBox.getSelectedIndex();
            if(selectedItem < 0) {
                return null;
            } else {
                return new EnumValue(enumItems.get(selectedItem).getId());
            }
        }

        @Override
        public void stop() {
            operandListBox.setVisible(false);
        }
    };



}
