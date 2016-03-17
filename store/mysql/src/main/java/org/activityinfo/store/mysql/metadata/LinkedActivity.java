package org.activityinfo.store.mysql.metadata;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;
import java.util.Collection;

/**
 * Stores information on activities that are linked to this activity
 */
public class LinkedActivity implements Serializable {
    
    int activityId;

    /**
     * Map from destination indicators to their source indicators.
     */
    final Multimap<Integer, Integer> linkMap = HashMultimap.create();


    int reportingFrequency;


    public int getActivityId() {
        return activityId;
    }

    public int getReportingFrequency() {
        return reportingFrequency;
    }

    public ResourceId getLeafFormClassId() {
        switch (reportingFrequency) {
            case 0:
                return CuidAdapter.activityFormClass(activityId);
            case 1:
                return CuidAdapter.reportingPeriodFormClass(activityId);
        }
        throw new IllegalStateException("reportingFrequency: " + reportingFrequency);   
    }

    public Collection<Integer> getSourceIndicatorIdsFor(int destinationIndicatorId) {
        return linkMap.get(destinationIndicatorId);
    }

    public ResourceId getSiteFormClassId() {
        return CuidAdapter.activityFormClass(activityId);
    }
}
