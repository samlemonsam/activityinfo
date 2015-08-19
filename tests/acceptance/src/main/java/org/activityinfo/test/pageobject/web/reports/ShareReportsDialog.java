package org.activityinfo.test.pageobject.web.reports;


import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;

import java.util.List;

public class ShareReportsDialog {
    
    private static final String DATABASE_NAME_COLUMN = "databaseName";
    private static final String SHARED_COLUMN = "visible";
    private static final String DEFAULT_DASHBOARD_COLUMN = "defaultDashboard";
    
    private GxtModal modal;
    private GxtGrid grid;

    public ShareReportsDialog(GxtModal modal) {
        this.modal = modal;
        this.grid =  GxtGrid.findGrids(modal.getWindowElement()).first().get().waitUntilReloadedSilently();
    }

    public List<String> getUserGroups() {
        return grid.columnValues(DATABASE_NAME_COLUMN);
    }
    
    public ShareReportsDialog shareWith(String databaseName, boolean shared) {
        GxtGrid.GxtCell sharedCell = grid.findCell(I18N.MESSAGES.databaseUserGroup(databaseName), SHARED_COLUMN);
        if(sharedCell.isChecked() != shared) {
            sharedCell.click();
        }
        return this;
    }
    
    public ShareReportsDialog putOnDashboard(String databaseName) {
        GxtGrid.GxtCell cell = grid.findCell(I18N.MESSAGES.databaseUserGroup(databaseName), DEFAULT_DASHBOARD_COLUMN);
        if(!cell.isChecked()) {
            cell.click();
        }
        return this;   
    }
    
    public void ok() {
        modal.accept(I18N.CONSTANTS.ok());
    }
}
