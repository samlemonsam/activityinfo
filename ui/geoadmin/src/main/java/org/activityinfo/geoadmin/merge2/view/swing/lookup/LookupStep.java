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
package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.mapping.ReferenceFieldMapping;
import org.activityinfo.geoadmin.merge2.view.swing.Step;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;


public class LookupStep implements Step {

    private final ImportView viewModel;
    private final ReferenceFieldMapping fieldMapping;

    public LookupStep(ImportView viewModel, ReferenceFieldMapping fieldMapping) {
        this.viewModel = viewModel;
        this.fieldMapping = fieldMapping;
    }

    @Override
    public String getLabel() {
        return fieldMapping.getTargetFieldLabel() + " Lookup";
    }

    @Override
    public StepPanel createView() {
        return new LookupStepPanel(viewModel, fieldMapping);
    }
}
