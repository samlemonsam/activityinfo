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
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yuriyz on 5/7/14.
 */
public class ValidateClassImportCommand implements ImportCommand<List<ValidationResult>> {

    private ImportCommandExecutor commandExecutor;

    @Nullable
    @Override
    public Promise<List<ValidationResult>> apply(@Nullable Void input) {
        return Promise.resolved(doClassValidation());
    }

    private List<ValidationResult> doClassValidation() {
        final ImportModel model = commandExecutor.getImportModel();
        final List<ValidationResult> validationResults = Lists.newArrayList();

        // Class based validation : check whether all mandatory fields has mapped
        for (FormTree.Node node : model.getFormTree().getRootFields()) {
            if (node.getField().isRequired() && model.getMapExistingActions(node.getField().getId()).isEmpty()) {
                final String fieldLabel = node.getField().getLabel();
                validationResults.add(ValidationResult.error(I18N.MESSAGES.fieldIsMandatory(fieldLabel)));
            }
        }
        return validationResults;
    }

    @Override
    public void setCommandExecutor(ImportCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

}


