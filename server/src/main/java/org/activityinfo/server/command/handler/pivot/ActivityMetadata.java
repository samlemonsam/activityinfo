package org.activityinfo.server.command.handler.pivot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ActivityMetadata {
    int id;
    int reportingFrequency;
    String name;
    String databaseName;
    int databaseId;
    String categoryName;
    int sortOrder;
    
    final Map<Integer, IndicatorMetadata> indicators = Maps.newHashMap();
    final Map<Integer, ActivityMetadata> linkedActivities = Maps.newHashMap();
    final List<IndicatorMetadata> linkedIndicators = Lists.newArrayList();
    
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
        List<IndicatorMetadata> indicators =  Lists.newArrayList();
        indicators.addAll(this.indicators.values());
        indicators.addAll(linkedIndicators);
        return indicators;
    }

    public ResourceId getFormClassId() {
        if(reportingFrequency == ActivityFormDTO.REPORT_MONTHLY) {
            return CuidAdapter.reportingPeriodFormClass(id);
        } else {
            return getSiteFormClassId();
        }
    }


    public ResourceId getSiteFormClassId() {
        return CuidAdapter.activityFormClass(id);
    }

    public Collection<ActivityMetadata> getLinkedActivities() {
        return linkedActivities.values();
    }


    public boolean isMonthly() {
        return reportingFrequency == ActivityFormDTO.REPORT_MONTHLY;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
