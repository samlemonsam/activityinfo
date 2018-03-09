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
package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.CountryDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;


class LocationTypeComboBox extends ComboBox<LocationTypeEntry> {

    public LocationTypeComboBox(Dispatcher service, CountryDTO country) {

        LocationTypeProxy proxy = new LocationTypeProxy(service, country.getId());
        ListLoader<ListLoadResult<?>> loader = new BaseListLoader<>(proxy);
        ListStore<LocationTypeEntry> store = new ListStore<>(loader);
        store.setModelComparer(new ModelComparer<LocationTypeEntry>() {
            @Override
            public boolean equals(LocationTypeEntry m1, LocationTypeEntry m2) {
                return m1.getId() == m2.getId();
            }
        });

        setAllowBlank(false);
        setEditable(false);
        setForceSelection(false);
        setStore(store);
        setTriggerAction(ComboBox.TriggerAction.ALL);
        setFieldLabel(I18N.CONSTANTS.locationType());
        setRenderer(new LocationTypeListRenderer());
        setItemSelector(LocationTypeListRenderer.getItemSelector());
        setValueField("id");
        setDisplayField("label");
    }

    @Override
    protected void onLoad(StoreEvent<LocationTypeEntry> se) {
        if (!isAttached() || !hasFocus()) {
            return;
        }
        if (store.getCount() > 0) {
            if (isExpanded()) {
                restrict();
            } else {
                expand();
            }
            // Select the list item NOT by label, but using the value
            if (!selectByValue(getValue())) {
                select(0);
            }
        } else {
            onEmptyResults();
        }
    }

    protected boolean selectByValue(LocationTypeEntry r) {
        if (r != null) {
            select(r);
            return true;
        }
        return false;
    }

    @Override
    public LocationTypeEntry getValue() {
        return this.value;
    }
}
