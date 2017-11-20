package org.activityinfo.ui.client.component.form.field;
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
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.ButtonWithIcon;
import org.activityinfo.ui.icons.Icons;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 09/28/2015.
 */
public class EnumCheckboxWidget implements FormFieldWidget<EnumValue> {

    public interface Templates extends SafeHtmlTemplates {

        // Note: the Left-to-right Mark (‎‎‎‎\u200E) is used here to ensure that the symbols [ | ] are correctly
        // interpretered as Left-to-right even if the text in between is RTL
        
        @Template("{0} <span class='enum-edit-controls'>\u200E[ <span class='enum-edit'>{1}</span> \u200E| <span class='enum-remove'>{2}</span> \u200E]</span>")
        SafeHtml designLabel(String itemLabel, String editLabel, String deleteLabel);

        @Template("{0} <span class='enum-edit-controls'/>")
        SafeHtml normalLabel(String label);

        @Template("<span class='enum-add'>\u200E+ {0}</span>")
        SafeHtml addChoice(String label);
    }


    private static int nextId = 1;


    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private EnumType enumType;

    private String groupName;
    private boolean readOnly;

    private final FlowPanel panel;
    private final FlowPanel boxPanel;
    private final List<CheckBox> controls;
    private final FieldWidgetMode fieldWidgetMode;
    private final ValueUpdater<EnumValue> valueUpdater;
    private final Button clearButton = createClearButton();

    public EnumCheckboxWidget(EnumType enumType, final ValueUpdater<EnumValue> valueUpdater, FieldWidgetMode fieldWidgetMode) {
        this.enumType = enumType;
        this.groupName = "group" + (nextId++);
        this.fieldWidgetMode = fieldWidgetMode;
        this.valueUpdater = valueUpdater;

        boxPanel = new FlowPanel();
        controls = new ArrayList<>();

        for (final EnumItem instance : enumType.getValues()) {
            CheckBox checkBox = createControl(instance);
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    fireValueChanged();
                }
            });
            boxPanel.add(checkBox);
        }

        panel = new FlowPanel();
        panel.addStyleName("hide-button-on-over");
        if (enumType.getCardinality() == Cardinality.SINGLE) {
            panel.add(clearButton);
        }
        panel.add(boxPanel);
        if (this.fieldWidgetMode == FieldWidgetMode.DESIGN) {
            panel.add(new HTML(TEMPLATES.addChoice(I18N.CONSTANTS.addItem())));
        }

        panel.sinkEvents(Event.MOUSEEVENTS);
        panel.addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                    onClick(event);
                }
            }
        }, MouseUpEvent.getType());
    }

    private Button createClearButton() {
        final Button clearButton = new Button();
        clearButton.setHTML(ButtonWithIcon.TEMPLATES.withIcon(Icons.INSTANCE.remove()));
        clearButton.setStyleName("btn btn-default btn-xs pull-right");
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (int i = 0; i < boxPanel.getWidgetCount(); i++) {
                    Widget widget = boxPanel.getWidget(i);
                    if (widget instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) widget;
                        if (checkBox.getValue() != null && checkBox.getValue()) {
                            checkBox.setValue(false, true);
                        }
                    }
                }
            }
        });
        clearButton.setVisible(false);
        return clearButton;
    }

    private void setClearButtonState(EnumValue enumValue) {
        clearButton.setVisible(enumValue != null && !enumValue.getResourceIds().isEmpty());
    }

    public void fireValueChanged() {
        EnumValue value = updatedValue();
        valueUpdater.update(value);
        setClearButtonState(value);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private void onClick(MouseUpEvent event) {
        Element target = event.getNativeEvent().getEventTarget().cast();
        if (target.getClassName().contains("enum-remove")) {
            remove(findId(target));
            fireValueChanged();
        } else if (target.getClassName().contains("enum-edit")) {
            ResourceId id = findId(target);
            editLabel(id);
            fireValueChanged();
        } else if (target.getClassName().contains("enum-add")) {
            addOption();
            fireValueChanged();
        }
    }

    private void addOption() {
        String newLabel = Window.prompt(I18N.CONSTANTS.enterNameForOption(), "");
        if (!Strings.isNullOrEmpty(newLabel)) {
            EnumItem newValue = new EnumItem(EnumItem.generateId(), newLabel);
            enumType.getValues().add(newValue);
            boxPanel.add(createControl(newValue));
        }
    }

    private void editLabel(ResourceId id) {
        EnumItem enumItem = enumValueForId(id);
        String newLabel = Window.prompt(I18N.CONSTANTS.enterNameForOption(), enumItem.getLabel());
        if (!Strings.isNullOrEmpty(newLabel)) {
            enumItem.setLabel(newLabel);
            controlForId(id).setHTML(designLabel(newLabel));
        }
    }

    private SafeHtml designLabel(String newLabel) {
        return TEMPLATES.designLabel(newLabel, I18N.CONSTANTS.edit(), I18N.CONSTANTS.delete());
    }

    private CheckBox controlForId(ResourceId id) {
        for (CheckBox checkBox : controls) {
            if (checkBox.getFormValue().equals(id.asString())) {
                return checkBox;
            }
        }
        throw new IllegalArgumentException(id.asString());
    }

    private EnumItem enumValueForId(ResourceId id) {
        for (EnumItem value : enumType.getValues()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException(id.asString());
    }

    private void remove(ResourceId id) {
        controlForId(id).removeFromParent();
        enumType.getValues().remove(enumValueForId(id));
    }

    private ResourceId findId(Element target) {
        Element container = target;
        while (Strings.isNullOrEmpty(container.getAttribute("data-id"))) {
            container = container.getParentElement();
        }
        return ResourceId.valueOf(container.getAttribute("data-id"));

    }

    private SafeHtml label(String label) {
        return fieldWidgetMode == FieldWidgetMode.DESIGN ? designLabel(label) : TEMPLATES.normalLabel(label);
    }

    private CheckBox createControl(EnumItem instance) {
        CheckBox checkBox;
        if (enumType.getCardinality() == Cardinality.SINGLE) {
            checkBox = new org.activityinfo.ui.client.widget.RadioButton(groupName, label(instance.getLabel()));
        } else {
            checkBox = new org.activityinfo.ui.client.widget.CheckBox(label(instance.getLabel()));
        }
        checkBox.setFormValue(instance.getId().asString());
        checkBox.getElement().setAttribute("data-id", instance.getId().asString());
        controls.add(checkBox);
        return checkBox;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;

        for (CheckBox control : controls) {
            control.setEnabled(!readOnly);
        }
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    private EnumValue updatedValue() {
        final Set<ResourceId> value = Sets.newHashSet();
        for (CheckBox control : controls) {
            if (control.getValue()) {
                value.add(ResourceId.valueOf(control.getFormValue()));
            }
        }
        return new EnumValue(value);
    }

    @Override
    public Promise<Void> setValue(EnumValue value) {
        for (CheckBox entry : controls) {
            entry.setValue(containsIgnoreCase(value.getResourceIds(), entry.getFormValue()));
        }
        setClearButtonState(value);
        return Promise.done();
    }

    /**
     * Check with ignoreCase, trick is that values from html form elemns sometimes are lowercased
     *
     * @param resourceIds
     * @return
     */
    private boolean containsIgnoreCase(Set<ResourceId> resourceIds, String resourceId) {
        for (ResourceId resource : resourceIds) {
            if (resource.asString().equalsIgnoreCase(resourceId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearValue() {
        setValue(EnumValue.EMPTY);
    }

    @Override
    public void setType(FieldType type) {
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
