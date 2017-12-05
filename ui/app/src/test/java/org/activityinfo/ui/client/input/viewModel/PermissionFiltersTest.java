package org.activityinfo.ui.client.input.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.store.testing.NfiForm;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PermissionFiltersTest {

    private TestSetup setup = new TestSetup();

    @Before
    public void setUp() throws Exception {
        LocaleProxy.initialize();
    }

    @Test
    public void simpleRootFieldTest() {

        NfiForm nfiForm = setup.getCatalog().getNfiForm();
        FormTree formTree = setup.getCatalog().getFormTree(nfiForm.getFormId());

        FormPermissions permissions = new FormPermissions();
        permissions.setViewFilter(nfiForm.getVillageField().getId() + " == 'g12345'");
        permissions.setUpdateFilter(nfiForm.getVillageField().getId() + "=='g12345'");

        PermissionFilters filters = new PermissionFilters(formTree, permissions);

        assertThat(filters.getReferenceBaseFilter(nfiForm.getVillageField().getId()).get().asExpression(),
                equalTo("[_id] == \"g12345\""));
    }

}