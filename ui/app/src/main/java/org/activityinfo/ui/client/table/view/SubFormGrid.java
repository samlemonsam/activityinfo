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

import com.google.common.base.Optional;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableUpdater;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

/**
 * Holds a sub form grid
 */
public class SubFormGrid extends VerticalLayoutContainer {

    private final Observable<EffectiveTableModel> tableModel;
    private TableViewModel viewModel;
    private ResourceId subFormId;
    private Subscription subscription;

    private TableGrid grid;

    public SubFormGrid(TableViewModel viewModel, ResourceId subFormId) {
        this.viewModel = viewModel;
        this.subFormId = subFormId;
        this.tableModel = viewModel.getEffectiveSubTable(subFormId);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        subscription = tableModel.subscribe(this::onModelChanged);
    }

    private void onModelChanged(Observable<EffectiveTableModel> model) {
        if(model.isLoaded()) {
            if(grid == null) {
                grid = new TableGrid(model.get(), model.get().getColumnSet(), new TableUpdater() {
                    @Override
                    public void updateFilter(Optional<FormulaNode> filterFormula) {
                        // TODO
                    }

                    @Override
                    public void updateColumnWidth(String columnId, int width) {
                        // TODO
                    }
                });
                add(grid, new VerticalLayoutData(1, 1));
            }
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        subscription.unsubscribe();
    }
}
