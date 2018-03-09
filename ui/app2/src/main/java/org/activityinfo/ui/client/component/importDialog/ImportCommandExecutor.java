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
package org.activityinfo.ui.client.component.importDialog;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.strategy.ColumnAccessor;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImporter;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImporterColumn;
import org.activityinfo.ui.client.component.importDialog.model.strategy.TargetSiteId;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 4/18/14.
 */
class ImportCommandExecutor {

    private final ImportModel importModel;
    private final List<Importer.TargetField> fields;
    private final List<FieldImporter> importers;
    private final List<FieldImporterColumn> columns;
    private final ResourceLocator resourceLocator;

    ImportCommandExecutor(ImportModel importModel, List<Importer.TargetField> fields, ResourceLocator resourceLocator) {
        this.importModel = importModel;
        this.fields = fields;
        this.resourceLocator = resourceLocator;
        this.importers = createImporters(importModel);
        this.columns = collectImporterColumns(importers);
    }

    public <T> Promise<T> execute(final ImportCommand<T> command) {
        command.setCommandExecutor(this);
        return Promise.forEach(importers, new Function<FieldImporter, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(FieldImporter input) {
                return input.prepare(resourceLocator, importModel.getSource().getRows());
            }
        }).join(command);
    }

    private List<FieldImporter> createImporters(final ImportModel model) {
        final List<FieldImporter> importers = Lists.newArrayList();
        for (Importer.TargetField field : fields) {
            Map<TargetSiteId, ColumnAccessor> mappedColumns = model.getMappedColumns(field.node.getFieldId());
            if (!mappedColumns.isEmpty()) {
                System.out.println(field + " => " + mappedColumns);

                FieldImporter importer = field.strategy.createImporter(field.node, mappedColumns, model);

                importers.add(importer);
            }
        }
        return importers;
    }

    private List<FieldImporterColumn> collectImporterColumns(List<FieldImporter> importers) {
        final List<FieldImporterColumn> collectTo = Lists.newArrayList();
        for (FieldImporter importer : importers) {
            collectTo.addAll(importer.getColumns());
        }
        return collectTo;
    }

    public List<Importer.TargetField> getFields() {
        return fields;
    }

    public List<FieldImporter> getImporters() {
        return importers;
    }

    public List<FieldImporterColumn> getColumns() {
        return columns;
    }

    public ImportModel getImportModel() {
        return importModel;
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }
}
