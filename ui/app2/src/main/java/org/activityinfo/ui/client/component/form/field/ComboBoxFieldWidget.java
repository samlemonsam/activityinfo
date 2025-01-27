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
package org.activityinfo.ui.client.component.form.field;

import com.google.common.collect.Sets;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yuriyz on 2/10/14.
 */
public class ComboBoxFieldWidget implements ReferenceFieldWidget {

    private final ListBox dropBox;
    private ResourceId formId;
    private final ValueUpdater<ReferenceValue> valueUpdater;

    public ComboBoxFieldWidget(ResourceId formId, final OptionSet range, final ValueUpdater<ReferenceValue> valueUpdater) {
        this.formId = formId;
        this.valueUpdater = valueUpdater;
        dropBox = new ListBox(false);
        dropBox.addStyleName("form-control");

        dropBox.addItem("", (String)null);

        for (int i = 0; i < range.getCount(); i++) {
            dropBox.addItem(
                    range.getLabel(i),
                    range.getRef(i).toQualifiedString());
        }
        dropBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                fireValueChanged();
            }
        });
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(updatedValue());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        dropBox.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return !dropBox.isEnabled();
    }

    private ReferenceValue updatedValue() {
        Set<RecordRef> refs = Sets.newHashSet();
        int selectedIndex = dropBox.getSelectedIndex();
        if(selectedIndex == 0) {
            return null;
        }
        if(selectedIndex != -1) {
            refs.add(RecordRef.fromQualifiedString(dropBox.getValue(selectedIndex)));
        }
        return new ReferenceValue(refs);
    }

    @Override
    public Promise<Void> setValue(ReferenceValue value) {

        Set<String> selectedRecords = new HashSet<>();
        for (RecordRef recordRef : value.getReferences()) {
            selectedRecords.add(recordRef.toQualifiedString());
        }

        if(selectedRecords.isEmpty()) {
            dropBox.setSelectedIndex(0);

        } else {
            for (int i = 0; i != dropBox.getItemCount(); ++i) {
                if (selectedRecords.contains(dropBox.getValue(i))) {
                    dropBox.setSelectedIndex(i);
                    break;
                }
            }
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
        return dropBox;
    }
}
