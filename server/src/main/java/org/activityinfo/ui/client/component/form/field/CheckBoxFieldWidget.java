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

import com.google.common.collect.Sets;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.*;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.RadioButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 2/11/14.
 */
public class CheckBoxFieldWidget implements ReferenceFieldWidget {

    private final FlowPanel panel;
    private final List<CheckBox> controls;
    private final ValueUpdater valueUpdater;
    private boolean readOnly;

    public CheckBoxFieldWidget(ReferenceType type, OptionSet range, final ValueUpdater valueUpdater) {
        this.valueUpdater = valueUpdater;
        panel = new FlowPanel();
        controls = new ArrayList<>();

        String groupId = Long.toString(new Date().getTime());
        for (int i = 0; i < range.getCount(); i++) {
            CheckBox checkBox = createControl(
                    groupId, 
                    range.getId(i),
                    range.getLabel(i),
                    type.getCardinality());
            
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    fireValueChanged();
                }
            });
            panel.add(checkBox);
            controls.add(checkBox);
        }

        // inform user that there is no any data on server for given formfield
        if (controls.isEmpty()) {
            panel.add(new HTML(FormFieldWidgetFactory.TEMPLATE.error(I18N.CONSTANTS.noDataForField())));

            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    fireValueChanged();
                }
            });
        }
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(updatedValue());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private CheckBox createControl(String groupId, String choiceId, String label, Cardinality cardinality) {
        final CheckBox checkBox;
        if (cardinality == Cardinality.SINGLE) {
            checkBox = new RadioButton(groupId, label);
        } else {
            checkBox = new CheckBox(label);
        }
        checkBox.setFormValue(choiceId);
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


    private ReferenceValue updatedValue() {
        final Set<RecordRef> value = Sets.newHashSet();
        for (CheckBox control : controls) {
            if (control.getValue()) {
                value.add(RecordRef.fromQualifiedString(control.getFormValue()));
            }
        }
        return new ReferenceValue(value);
    }

    @Override
    public Promise<Void> setValue(ReferenceValue value) {
        Set<RecordRef> ids = value.getReferences();
        for (CheckBox entry : controls) {
            RecordRef ref = RecordRef.fromQualifiedString(entry.getFormValue());
            entry.setValue(ids.contains(ref));
        }
        return Promise.done();
    }

    @Override
    public void clearValue() {
        setValue(ReferenceValue.EMPTY);
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
