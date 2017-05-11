package org.activityinfo.ui.client.component.table.action;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.icons.Icons;

import java.util.logging.Logger;

/**
 * Created by yuriyz on 9/5/2016.
 */
public class ExportHeaderAction implements TableHeaderAction {

    public static final Logger LOGGER = Logger.getLogger(ActivityInfoClientAsync.class.getName());

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
        UrlBuilder urlBuilder = urlBuilder()
                .setPath("/resources/form/" + table.getRootFormClass().getId().asString()
                        + "/query/columns.xls");

//        for (FieldColumn column : table.getColumns()) {
//            String id = column.get().getNode().getFieldId().asString();
//            urlBuilder.setParameter(id, id);
//        }

        Window.open(urlBuilder.buildString(), "_blank", "");
    }

    private UrlBuilder urlBuilder() {
        UrlBuilder builder = new UrlBuilder();
        builder.setProtocol(Window.Location.getProtocol());
        builder.setHost(Window.Location.getHost());

        String port = Window.Location.getPort();
        if (port != null && port.length() > 0) {
            builder.setPort(Integer.parseInt(port));
        }
        return builder;
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
