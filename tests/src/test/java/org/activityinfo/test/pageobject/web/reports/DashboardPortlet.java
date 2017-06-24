package org.activityinfo.test.pageobject.web.reports;

import cucumber.api.DataTable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.gxt.GxtGrid;


public class DashboardPortlet {
    private FluentElement container;

    public DashboardPortlet(FluentElement container) {
        this.container = container;
    }
    
    public DataTable extractPivotTableData() {
        return GxtGrid.waitForGrids(container).first().get().extractData();
    }
}
