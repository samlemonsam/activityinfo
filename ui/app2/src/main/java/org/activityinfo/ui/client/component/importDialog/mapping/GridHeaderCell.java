package org.activityinfo.ui.client.component.importDialog.mapping;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.importDialog.model.ColumnAction;
import org.activityinfo.ui.client.component.importDialog.model.IgnoreAction;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.MapExistingAction;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

/**
 * Handles click events on header cells
 */
class GridHeaderCell extends AbstractCell<SourceColumn> {

    private ImportModel model;

    public GridHeaderCell(ImportModel model) {
        super(CLICK);
        this.model = model;
    }

    @Override
    public void render(Context context, SourceColumn column, SafeHtmlBuilder sb) {
        if (context.getIndex() == ColumnMappingGrid.SOURCE_COLUMN_HEADER_ROW) {
            sb.append(SafeHtmlUtils.fromTrustedString("<span title='" + column.getHeader() + "'>"));
            sb.appendEscaped(column.getHeader());
            sb.append(SafeHtmlUtils.fromTrustedString("</span>"));
        } else {
            ColumnAction action = model.getColumnAction(column);
            if (action == null) {
                sb.appendHtmlConstant(I18N.CONSTANTS.chooseFieldHeading());
            } else if (action == IgnoreAction.INSTANCE) {
                sb.appendEscaped(I18N.CONSTANTS.ignoreColumnAction());
            } else if (action instanceof MapExistingAction) {
                String label = ((MapExistingAction) action).getTarget().getLabel();

                sb.append(SafeHtmlUtils.fromTrustedString("<span title='" + label + "'>"));
                sb.appendEscaped(label);
                sb.append(SafeHtmlUtils.fromTrustedString("</span>"));
            }
        }
    }
}
