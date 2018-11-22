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

import com.google.common.collect.Lists;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromisesExecutionMonitor;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImporter;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yuriyz on 4/18/14.
 */
public class PersistImportCommand implements ImportCommand<Void> {


    private ImportCommandExecutor commandExecutor;
    private PromisesExecutionMonitor monitor;

    public PersistImportCommand(PromisesExecutionMonitor monitor) {
        this.monitor = monitor;
    }

    @Nullable
    @Override
    public Promise<Void> apply(Void input) {
        final ImportModel model = commandExecutor.getImportModel();

        final ResourceId formClassId = model.getFormTree().getRootFields().iterator().next().getDefiningFormClass().getId();
        final List<TypedFormRecord> toPersist = Lists.newArrayList();
        final ValidatedRowTable validatedRowTable = model.getValidatedRowTable();

        for (SourceRow row : model.getSource().getRows()) {
            ValidatedRow validatedRow = validatedRowTable.getRow(row);
            if (validatedRow.isValid()) { // persist instance only if it's valid
                // new instance per row
                TypedFormRecord newInstance = new TypedFormRecord(CuidAdapter.newLegacyFormInstanceId(formClassId), formClassId);
                for (FieldImporter importer : commandExecutor.getImporters()) {
                    importer.updateInstance(row, newInstance);
                }
                toPersist.add(newInstance);
            }
        }

        SerialQueue queue = new SerialQueue(commandExecutor.getResourceLocator(), toPersist, monitor);
        return queue.execute();
    }

    @Override
    public void setCommandExecutor(ImportCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
}
