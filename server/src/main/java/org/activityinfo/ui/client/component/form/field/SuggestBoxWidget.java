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

import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.suggest.InstanceSuggestOracle;
import org.activityinfo.ui.client.component.form.field.suggest.ReferenceSuggestion;
import org.activityinfo.ui.client.widget.SuggestBox;

import java.util.Objects;

/**
 * @author yuriyz on 2/10/14.
 */
public class SuggestBoxWidget implements ReferenceFieldWidget {

    private final SuggestBox suggestBox;

    private RecordRef value;
    private ResourceId formId;
    private OptionSet range;
    private ReferenceSuggestion selectedSuggestion;
    private final ValueUpdater<ReferenceValue> valueUpdater;

    public SuggestBoxWidget(ResourceId formId, OptionSet instances, final ValueUpdater<ReferenceValue> valueUpdater) {
        this.formId = formId;
        this.range = instances;
        this.valueUpdater = valueUpdater;
        this.suggestBox = new SuggestBox(new InstanceSuggestOracle(formId, instances));
        this.suggestBox.setPlaceholder(I18N.CONSTANTS.suggestBoxPlaceholder());
        this.suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                selectedSuggestion = (ReferenceSuggestion) event.getSelectedItem();
                if(!Objects.equals(selectedSuggestion.getRef(), value)) {
                    fireValueChanged();
                }
            }
        });
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(selectedSuggestion != null ?
                new ReferenceValue(selectedSuggestion.getRef()) :
                new ReferenceValue());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        suggestBox.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return !suggestBox.isEnabled();
    }

    @Override
    public Promise<Void> setValue(ReferenceValue value) {
        RecordRef newValue = Iterables.getFirst(value.getReferences(), null);
        if(!Objects.equals(newValue, this.value)) {
            this.value = newValue;
            if(newValue == null) {
                suggestBox.setValue(null);
            } else {
                suggestBox.setValue(findDisplayLabel(newValue));
            }
        }
        return Promise.done();
    }

    @Override
    public void clearValue() {
        suggestBox.setValue(null);
    }

    @Override
    public void setType(FieldType type) {

    }

    private String findDisplayLabel(RecordRef newValue) {
        for (int i = 0; i < range.getCount(); i++) {
            if(range.getRef(i).equals(newValue)) {
                return range.getLabel(i);
            }
        }
        return newValue.toQualifiedString();
    }

    @Override
    public Widget asWidget() {
        return suggestBox;
    }
}
