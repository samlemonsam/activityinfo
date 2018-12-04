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
package org.activityinfo.ui.client.table.view;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.HashMap;
import java.util.Map;

/**
 * Hosts sub form tabs
 */
public class SubFormPane extends TabPanel {

    private TableViewModel viewModel;
    private Subscription subscription;

    private SubFormGrid activeGrid = null;

    private final Map<ResourceId, TabItemConfig> tabs = new HashMap<>();

    public SubFormPane(TableViewModel viewModel, FormTree formTree) {
        this.viewModel = viewModel;
        updateTabs(formTree);
        addSelectionHandler(new SelectionHandler<Widget>() {
            @Override
            public void onSelection(SelectionEvent<Widget> event) {

                SubFormGrid newlyActiveGrid = (SubFormGrid) event.getSelectedItem();

                if(activeGrid != newlyActiveGrid) {
                    if(activeGrid != null) {
                        activeGrid.setActive(false);
                        activeGrid = null;
                    }
                }

                // If there is an update to the ColumnSet while the grid is not visible
                // (for example, when the parent selection changes)
                // The LiveGridView's DOM measurements get screwed up, and the updates are not
                // properly "painted".
                // To work around this, we need to force the LiveGridView to refresh when a new
                // tab is selected.
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        newlyActiveGrid.setActive(true);
                        activeGrid = newlyActiveGrid;
                    }
                });
            }
        });
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        subscription = viewModel.getFormTree().subscribe(this::onFormTreeChanged);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        subscription.unsubscribe();
    }

    private void onFormTreeChanged(Observable<FormTree> formTree) {
        if(formTree.isLoaded()) {
            updateTabs(formTree.get());
        }
    }

    private void updateTabs(FormTree formTree) {
        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.isSubForm() && node.isSubFormVisible()) {
                addOrUpdateTab(formTree, node);
            }
        }
    }

    private void addOrUpdateTab(FormTree formTree, FormTree.Node node) {

        TabItemConfig tabItemConfig = tabs.get(node.getFieldId());
        if(tabItemConfig == null) {
            SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
            FormClass subForm = formTree.getFormClass(subFormType.getClassId());
            TabItemConfig config = new TabItemConfig(subForm.getLabel());

            SubFormGrid subFormGrid = new SubFormGrid(viewModel, subForm.getId());

            if(tabs.isEmpty()) {
                // First tab is by default active
                subFormGrid.setActive(true);
                this.activeGrid = subFormGrid;
            }

            tabs.put(node.getFieldId(), config);
            add(subFormGrid, config);
        }
    }

}
