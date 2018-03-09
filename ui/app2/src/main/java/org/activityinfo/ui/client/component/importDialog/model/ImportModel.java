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
package org.activityinfo.ui.client.component.importDialog.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumnAccessor;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceTable;
import org.activityinfo.ui.client.component.importDialog.model.strategy.ColumnAccessor;
import org.activityinfo.ui.client.component.importDialog.model.strategy.TargetSiteId;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;

import java.util.Map;

/**
 * A model which defines the mapping from an {@code SourceTable}
 * to a FormClass
 */
public class ImportModel {

    private SourceTable source;
    private FormTree formTree;
    private ValidatedRowTable validatedRowTable;

    private final Map<SourceColumn, ColumnAction> columnActions = Maps.newHashMap();


    public ImportModel(FormTree formTree) {
        this.formTree = formTree;
    }

    public void setSource(SourceTable source) {
        this.source = source;
        this.columnActions.clear();
    }

    public SourceTable getSource() {
        return source;
    }

    public SourceColumn setColumnBinding(ColumnAction action, SourceColumn sourceColumn) {
        SourceColumn removedColumn = null;
        for (Map.Entry<SourceColumn, ColumnAction> entry : Sets.newHashSet(columnActions.entrySet())) {
            final ColumnAction value = entry.getValue();
            if (value != null && value.equals(action) && value != IgnoreAction.INSTANCE) {
                removedColumn = entry.getKey();
                columnActions.remove(removedColumn);
            }
        }
        columnActions.put(sourceColumn, action);
        return removedColumn;
    }


    public FormTree getFormTree() {
        return formTree;
    }

    public SourceColumn getSourceColumn(int columnIndex) {
        return source.getColumns().get(columnIndex);
    }

    public Map<SourceColumn, ColumnAction> getColumnActions() {
        return columnActions;
    }

    public Map<TargetSiteId, ColumnAccessor> getMappedColumns(ResourceId fieldId) {
        Map<TargetSiteId, ColumnAccessor> mappings = Maps.newHashMap();
        for (Map.Entry<SourceColumn, MapExistingAction> entry : getMapExistingActions(fieldId).entrySet()) {
            TargetSiteId site = entry.getValue().getTarget().getSite();
            ColumnAccessor column = new SourceColumnAccessor(entry.getKey());
            mappings.put(site, column);
        }
        return mappings;
    }

    public Map<SourceColumn, MapExistingAction> getMapExistingActions(ResourceId fieldId) {
        Map<SourceColumn, MapExistingAction> existingActions = Maps.newHashMap();
        for (Map.Entry<SourceColumn, ColumnAction> entry : columnActions.entrySet()) {
            if (entry.getValue() instanceof MapExistingAction) {
                MapExistingAction action = (MapExistingAction) entry.getValue();
                if (action.getTarget().getFormField().getId().equals(fieldId)) {
                    existingActions.put(entry.getKey(), action);
                }
            }
        }
        return existingActions;
    }

    public void setColumnAction(int columnIndex, ColumnAction target) {
        columnActions.put(source.getColumns().get(columnIndex), target);
    }

    public ColumnAction getColumnAction(SourceColumn column) {
        return columnActions.get(column);
    }

    public ValidatedRowTable getValidatedRowTable() {
        return validatedRowTable;
    }

    public void setValidatedRowTable(ValidatedRowTable validatedRowTable) {
        this.validatedRowTable = validatedRowTable;
    }
}
