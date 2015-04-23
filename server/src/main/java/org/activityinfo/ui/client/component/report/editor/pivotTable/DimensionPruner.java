package org.activityinfo.ui.client.component.report.editor.pivotTable;

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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.GetActivityForms;
import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.legacy.shared.reports.model.AttributeGroupDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.report.HasReportElement;
import org.activityinfo.ui.client.page.report.ReportChangeHandler;
import org.activityinfo.ui.client.page.report.ReportEventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Removes inapplicable dimensions from the model after a user change.
 * <p/>
 * <p/>
 * For example, if an attribute dimension related to activity X is selected, but
 * all indicators from Activity are removed, then we need to remove the
 * dimension.
 */
public class DimensionPruner implements HasReportElement<PivotTableReportElement> {

    private static final Logger LOGGER = Logger.getLogger(DimensionPruner.class.getName());

    private final ReportEventBus reportEventBus;
    private PivotTableReportElement model;
    private Dispatcher dispatcher;

    private Map<Integer, String> groupNames = Maps.newHashMap();

    @Inject
    public DimensionPruner(EventBus eventBus, Dispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
        this.reportEventBus = new ReportEventBus(eventBus, this);
        this.reportEventBus.listen(new ReportChangeHandler() {

            @Override
            public void onChanged() {
                DimensionPruner.this.onChanged();
            }
        });
    }

    protected void onChanged() {
        dispatcher.execute(new GetActivityForms(model.getFilter()), new AsyncCallback<ActivityFormResults>() {

            @Override
            public void onFailure(Throwable caught) {
                Log.error(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(ActivityFormResults result) {
                pruneModel(result);
            }
        });
    }

    private void pruneModel(ActivityFormResults schema) {

        Map<Integer, String> valid = validDimensions(schema);
        Set<String> validLabels = new HashSet<>(valid.values());

        boolean dirty = false;
        for (AttributeGroupDimension selectedDim : getSelectedAttributes()) {
            String label = groupNames.get(selectedDim.getAttributeGroupId());
            if (!validLabels.contains(label)) {

                LOGGER.fine("Removing attribute group " + selectedDim.getAttributeGroupId());
                model.getRowDimensions().remove(selectedDim);
                model.getColumnDimensions().remove(selectedDim);
                dirty = true;
            }
        }
        if (dirty) {
            reportEventBus.fireChange();
        }

        // Store group labels so that we can match by name of those
        // dimensions which might no longer be returned by GetActivityForms(filter)
        groupNames.putAll(valid);
    }

    private Map<Integer, String> validDimensions(ActivityFormResults schema) {
        Map<Integer, String> valid = new HashMap<>();

        for(ActivityFormDTO form : schema.getData()) {
            for(AttributeGroupDTO group : form.getAttributeGroups()) {
                valid.put(group.getId(), group.getName());
            }
        }
        return valid;
    }

    private Set<AttributeGroupDimension> getSelectedAttributes() {
        Set<AttributeGroupDimension> dimensions = Sets.newHashSet();
        for (Dimension dim : model.allDimensions()) {
            if (dim instanceof AttributeGroupDimension) {
                dimensions.add((AttributeGroupDimension) dim);
            }
        }
        return dimensions;
    }


    @Override
    public void bind(PivotTableReportElement model) {
        this.model = model;
    }

    @Override
    public PivotTableReportElement getModel() {
        return model;
    }

    @Override
    public void disconnect() {
        reportEventBus.disconnect();
    }
}
