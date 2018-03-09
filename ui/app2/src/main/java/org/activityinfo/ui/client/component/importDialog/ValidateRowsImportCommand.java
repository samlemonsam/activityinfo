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
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImporter;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yuriyz on 4/18/14.
 */
public class ValidateRowsImportCommand implements ImportCommand<ValidatedRowTable> {

    private ImportCommandExecutor commandExecutor;

    @Nullable
    @Override
    public Promise<ValidatedRowTable> apply(@Nullable Void input) {
        return Promise.resolved(doRowValidation());
    }

    private ValidatedRowTable doRowValidation() {
        final List<ValidatedRow> rows = Lists.newArrayList();
        final ImportModel model = commandExecutor.getImportModel();

        // Row based validation
        for (SourceRow row : model.getSource().getRows()) {
            List<ValidationResult> results = Lists.newArrayList();
            for (FieldImporter importer : commandExecutor.getImporters()) {
                importer.validateInstance(row, results);
            }
            rows.add(new ValidatedRow(row, results));
        }
        ValidatedRowTable validatedRowTable = new ValidatedRowTable(commandExecutor.getColumns(), rows);
        model.setValidatedRowTable(validatedRowTable);
        return validatedRowTable;
    }

    @Override
    public void setCommandExecutor(ImportCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
}
