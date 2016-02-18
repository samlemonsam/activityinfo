package org.activityinfo.ui.client.component.importDialog.mapping;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import org.activityinfo.core.shared.importing.source.SourceColumn;
import org.activityinfo.core.shared.importing.source.SourceRow;

class GridColumn extends Column<SourceRow, String> {
    private SourceColumn column;

    public GridColumn(SourceColumn column) {
        super(new TextCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.append(SafeHtmlUtils.fromTrustedString("<div title='" + value.asString() + "'>"));
                    sb.append(value);
                    sb.append(SafeHtmlUtils.fromTrustedString("</div>"));
                }
            }
        });
        this.column = column;
    }

    @Override
    public String getValue(SourceRow row) {
        return row.getColumnValue(column.getIndex());
    }
}
