package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.reports.model.ReportElement;

/**
 * Generates the content of a saved report by Id. Similar to {@link GenerateElement},
 * but rather than sending the report definition to the server, we send only the id,
 * and return the report definition AND content in one request.
 */
public class GenerateDashboardElement implements Command<ReportElement> {
    
    private int reportId;

    public GenerateDashboardElement() {
    }

    public GenerateDashboardElement(int reportId) {
        this.reportId = reportId;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
}
