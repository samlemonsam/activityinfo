package org.activityinfo.ui.client.component.importDialog.validation.cells;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

/**
 * Displays a read-only value mapped form the {@code SourceTable}
 */
public class MappedDataCell extends AbstractSafeHtmlCell<Object> {

    public MappedDataCell(SafeHtmlRenderer renderer) {
        super(renderer);
    }

    @Override
    protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        if(data != null) {
            sb.append(data);
        }
    }
}
