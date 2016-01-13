package org.activityinfo.test.pageobject.web.reports;

import org.activityinfo.test.pageobject.gxt.GxtGrid;

import java.util.List;


public class ReportsList {
    private GxtGrid grid;

    public ReportsList(GxtGrid grid) {
        this.grid = grid;
    }
    
    public List<String> getTitles() {
        return  grid.columnValues("title");
    }
}
