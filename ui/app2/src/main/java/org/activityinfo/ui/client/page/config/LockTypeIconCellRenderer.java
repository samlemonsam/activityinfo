package org.activityinfo.ui.client.page.config;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

class LockTypeIconCellRenderer implements GridCellRenderer<LockedPeriodDTO> {
    @Override
    public SafeHtml render(LockedPeriodDTO model,
                           String property,
                           ColumnData config,
                           int rowIndex,
                           int colIndex,
                           ListStore<LockedPeriodDTO> store,
                           Grid<LockedPeriodDTO> grid) {

        if (model.getParent() instanceof IsActivityDTO) {
            return IconImageBundle.ICONS.form().getSafeHtml();
        }

        if (model.getParent() instanceof UserDatabaseDTO) {
            return IconImageBundle.ICONS.database().getSafeHtml();
        }

        if (model.getParent() instanceof ProjectDTO) {
            return IconImageBundle.ICONS.project().getSafeHtml();
        }

        if (model.getParent() instanceof FolderDTO) {
            return IconImageBundle.ICONS.folder().getSafeHtml();
        }

        return SafeHtmlUtils.EMPTY_SAFE_HTML;
    }
}
