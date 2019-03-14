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
package org.activityinfo.ui.client.component.report.editor.map.layerOptions;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.ui.client.component.filter.FilterResources;
import org.activityinfo.ui.client.component.filter.FilterWidget;
import org.activityinfo.ui.client.component.filter.SelectionCallback;
import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PartnerFilterWidget extends FilterWidget {

    private Dispatcher dispatcher;
    private PartnerFilterDialog dialog;

    public PartnerFilterWidget(Dispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
        this.dimensionSpan.setInnerText(I18N.CONSTANTS.partners());
        this.stateSpan.setInnerText(I18N.CONSTANTS.all());
    }

    @Override
    public void choose(Event event) {
        if (dialog == null) {
            dialog = new PartnerFilterDialog(dispatcher);
        }
        dialog.show(getBaseFilter(), getValue(), new SelectionCallback<Set<Integer>>() {

            @Override
            public void onSelected(Set<Integer> selection) {
                Filter newValue = new Filter();
                if (selection != null && !selection.isEmpty()) {
                    newValue.addRestriction(DimensionType.Partner, selection);
                }
                setValue(newValue);
            }
        });
    }

    @Override
    public void updateView() {
        if (getValue().isRestricted(DimensionType.Partner)) {
            setState(I18N.CONSTANTS.loading());
            retrievePartnerNames();
        } else {
            setState(I18N.CONSTANTS.all());
        }
    }

    private void retrievePartnerNames() {
        dispatcher.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(SchemaDTO result) {
                formatPartners(result);
            }
        });
    }

    private void formatPartners(SchemaDTO schema) {
        List<String> partnerNames = new ArrayList<String>();
        for (Integer id : getValue().getRestrictions(DimensionType.Partner)) {
            partnerNames.add(schema.getPartnerById(id).getName());
        }
        Collections.sort(partnerNames);
        setState(FilterResources.MESSAGES.filteredPartnerList(partnerNames));
    }
}
