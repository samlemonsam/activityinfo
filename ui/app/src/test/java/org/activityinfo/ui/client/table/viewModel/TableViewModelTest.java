package org.activityinfo.ui.client.table.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.observable.Connection;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Test;

import static org.activityinfo.observable.ObservableTesting.connect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TableViewModelTest {

    @Before
    public void setup() {
        LocaleProxy.initialize();
    }


    @Test
    public void test() {
        TableModel tableModel = ImmutableTableModel.builder()
                .formId(Survey.FORM_ID)
                .build();

        TestingFormStore formStore = new TestingFormStore();
        TableViewModel model = new TableViewModel(formStore, tableModel);

        Connection<EffectiveTableModel> view = connect(model.getEffectiveTable());
        EffectiveTableModel effectiveTableModel = view.assertLoaded();

        EffectiveTableColumn nameColumn = effectiveTableModel.getColumns().get(0);
        assertThat(nameColumn.getLabel(), equalTo("Respondent Name"));
    }

    @Test
    public void testDeleted() {

        TableModel tableModel = ImmutableTableModel.builder()
                .formId(Survey.FORM_ID)
                .build();

        TestingFormStore formStore = new TestingFormStore();
        formStore.deleteForm(Survey.FORM_ID);

        TableViewModel model = new TableViewModel(formStore, tableModel);

        Connection<EffectiveTableModel> view = connect(model.getEffectiveTable());
        EffectiveTableModel effectiveTableModel = view.assertLoaded();

        assertThat(effectiveTableModel.getFormTree().getRootState(), equalTo(FormTree.State.DELETED));

    }


}