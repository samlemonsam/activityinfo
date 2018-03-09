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

    private final Map<ResourceId, TabItemConfig> tabs = new HashMap<>();

    public SubFormPane(TableViewModel viewModel, FormTree formTree) {
        this.viewModel = viewModel;
        updateTabs(formTree);
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
            tabs.put(node.getFieldId(), config);
            add(new SubFormGrid(viewModel, subForm.getId()), config);
        }
    }

}
