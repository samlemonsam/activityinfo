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
package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.store.testing.NfiForm;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class OperationFiltersTest {

    private TestSetup setup = new TestSetup();

    @Test
    public void simpleRootFieldTest() {

        NfiForm nfiForm = setup.getCatalog().getNfiForm();
        FormTree formTree = setup.getCatalog().getFormTree(nfiForm.getFormId());

        FormPermissions permissions = new FormPermissions.Builder()
                .allowFilteredView(nfiForm.getVillageField().getId() + " == 'g12345'")
                .allowFilteredEdit(nfiForm.getVillageField().getId() + "=='g12345'")
                .build();

        PermissionFilters filters = new PermissionFilters(formTree, permissions);

        assertThat(filters.getReferenceBaseFilter(nfiForm.getVillageField().getId()).get().asExpression(),
                equalTo("[_id] == \"g12345\""));
    }

}