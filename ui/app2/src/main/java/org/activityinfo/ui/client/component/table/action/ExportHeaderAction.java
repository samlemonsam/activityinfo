package org.activityinfo.ui.client.component.table.action;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.job.ExportColumn;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.page.report.ExportDialog;
import org.activityinfo.ui.icons.Icons;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by yuriyz on 9/5/2016.
 */
public class ExportHeaderAction implements TableHeaderAction {

    private static final Logger LOGGER = Logger.getLogger(ExportHeaderAction.class.getName());

    private final InstanceTable table;
    private final String uniqueId;

    public ExportHeaderAction(InstanceTable table) {
        this.table = table;
        this.uniqueId = Document.get().createUniqueId();
    }

    @Override
    public void execute() {
        export();
    }

    public void export() {

        List<ExportColumn> columns = new ArrayList<>();
        for (FieldColumn column : table.getColumns()) {
            columns.add(new ExportColumn(column.get().getExpr()));
        }

        ExportFormJob job = new ExportFormJob(table.getRootFormClass().getId(), columns);

        ExportDialog dialog = new ExportDialog();
        dialog.start(new ExportJobTask(job));
    }

    @Override
    public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
        sb.append(TEMPLATE.enabled(uniqueId, Icons.INSTANCE.excelFile(), I18N.CONSTANTS.export()));
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

}
