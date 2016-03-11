package org.activityinfo.ui.client.component.table.action;

import com.google.common.base.Optional;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.page.print.PrintFormPanel;
import org.activityinfo.ui.icons.Icons;

import java.util.Set;

/**
 * Opens the print view for a single form
 */
public class PrintFormAction implements TableHeaderAction {


    private final InstanceTable table;
    private final String uniqueId;

    public PrintFormAction(InstanceTable table) {
        this.table = table;
        this.uniqueId = Document.get().createUniqueId();
    }

    @Override
    public void execute() {
        Optional<ResourceId> selection = getUniqueSelection();
        if(selection.isPresent()) {
            PrintFormPanel.open(selection.get());
        }
    }

    @Override
    public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
        if (getUniqueSelection().isPresent()) {
            sb.append(TEMPLATE.enabled(uniqueId, Icons.INSTANCE.print(), I18N.CONSTANTS.printForm()));
        } else {
            sb.append(TEMPLATE.disabled(uniqueId, Icons.INSTANCE.print(), I18N.CONSTANTS.printForm()));
        }
    }
    
    private Optional<ResourceId> getUniqueSelection() {
        Set<Projection> set = table.getSelectionModel().getSelectedSet();
        if(set.size() == 1) {
            return Optional.of(set.iterator().next().getRootInstanceId());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }


  
}
