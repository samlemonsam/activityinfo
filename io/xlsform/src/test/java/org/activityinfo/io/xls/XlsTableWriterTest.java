package org.activityinfo.io.xls;

import com.google.common.io.Resources;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.store.query.server.FormSourceSyncImpl;
import org.activityinfo.store.testing.TestingStorageProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import static bad.robot.excel.matchers.Matchers.sameWorkbook;
import static org.hamcrest.MatcherAssert.assertThat;


public class XlsTableWriterTest {

    private FormSourceSyncImpl formSource;
    private TestingStorageProvider catalog;

    @Before
    public void setup() {
        LocaleProxy.initialize();

        catalog = new TestingStorageProvider();
        formSource = new FormSourceSyncImpl(catalog, 1);
    }

    @Test
    public void surveyForm() throws IOException {

        TableModel tableModel = ImmutableTableModel.builder()
                .formId(catalog.getSurvey().getFormId())
                .build();

        assertThat(export(tableModel), sameWorkbook(getWorkBook("survey-expected.xls")));
    }

    private HSSFWorkbook export(TableModel tableModel) throws IOException {

        TableViewModel viewModel = new TableViewModel(formSource, tableModel);
        EffectiveTableModel effectiveTableModel = viewModel.getEffectiveTable().waitFor();

        if(effectiveTableModel.getRootFormState() != FormTree.State.VALID) {
            throw new IllegalStateException("Root Form has state: " + effectiveTableModel.getRootFormState());
        }

        ColumnSet columnSet = effectiveTableModel.getColumnSet().waitFor();

        XlsTableWriter writer = new XlsTableWriter();
        writer.addSheet(effectiveTableModel, columnSet);
        writer.write(new FileOutputStream("build/" + tableModel.getFormId().asString() + ".xls"));

        return writer.getBook();
    }

    private Workbook getWorkBook(String name) throws IOException {
        URL url = Resources.getResource(XlsTableWriterTest.class, name);
        HSSFWorkbook workbook = new HSSFWorkbook(url.openStream());
        return workbook;
    }
}