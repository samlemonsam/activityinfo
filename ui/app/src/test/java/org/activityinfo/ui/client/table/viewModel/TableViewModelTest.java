package org.activityinfo.ui.client.table.viewModel;

import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.activityinfo.ui.client.table.model.ImmutableTableModel;
import org.activityinfo.ui.client.table.model.TableModel;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TableViewModelTest {

    @Test
    public void test() {
        TableModel tableModel = ImmutableTableModel.builder()
                .formId(Survey.FORM_ID)
                .build();

        TestingFormStore formStore = new TestingFormStore();
        TableViewModel model = new TableViewModel(formStore, tableModel);

        EffectiveTableModel effectiveTableModel = model.getEffectiveTable().get();
        EffectiveTableColumn nameColumn = effectiveTableModel.getColumns().get(0);
        assertThat(nameColumn.getLabel(), equalTo("Respondent Name"));
    }


}