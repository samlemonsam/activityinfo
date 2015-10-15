package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.List;

public class ActivityMetadata {
    int id;
    int reportingFrequency;
    String name;
    String databaseName;
    int databaseId;
    String categoryName;
    
    List<IndicatorMetadata> indicators = new ArrayList<>();

    public int getId() {
        return id;
    }

    public int getReportingFrequency() {
        return reportingFrequency;
    }

    public String getName() {
        return name;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public List<IndicatorMetadata> getIndicators() {
        return indicators;
    }

    public ResourceId getFormClassId() {
        if(reportingFrequency == ActivityFormDTO.REPORT_MONTHLY) {
            return CuidAdapter.reportingPeriodFormClass(id);
        } else {
            return CuidAdapter.activityFormClass(id);
        }
    }
}
