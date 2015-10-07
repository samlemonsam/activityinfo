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
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.suggest.EnumItemSuggestOracle;
import org.activityinfo.ui.client.component.form.field.suggest.Suggestion;
import org.activityinfo.ui.client.widget.SuggestBox;

import java.util.Objects;

/**
 * @author yuriyz on 09/28/2015.
 */
public class EnumSuggestBoxWidget implements FormFieldWidget<EnumValue> {

    private ResourceId value;
    private final EnumType enumType;
    private final SuggestBox suggestBox;

    public EnumSuggestBoxWidget(EnumType enumType, final ValueUpdater<EnumValue> valueUpdater) {
        this.enumType = enumType;
        this.suggestBox = new SuggestBox(new EnumItemSuggestOracle(enumType.getValues()));
        this.suggestBox.setPlaceholder(I18N.CONSTANTS.suggestBoxPlaceholder());
        this.suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                Suggestion suggestion = (Suggestion) event.getSelectedItem();
                if (!Objects.equals(suggestion.getId(), value)) {
                    valueUpdater.update(new EnumValue(suggestion.getId()));
                }
            }
        });
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        suggestBox.setEnabled(!readOnly);
    }

    @Override
    public Promise<Void> setValue(EnumValue value) {
        ResourceId newValue = Iterables.getFirst(value.getResourceIds(), null);
        if (!Objects.equals(newValue, this.value)) {
            this.value = newValue;
            if (newValue == null) {
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

    private String findDisplayLabel(ResourceId newValue) {
        for (EnumItem enumItem : enumType.getValues()) {
            if (enumItem.getId().equals(newValue)) {
                return enumItem.getLabel();
            }
        }
        return newValue.asString();
    }

    @Override
    public Widget asWidget() {
        return suggestBox;
    }

}
