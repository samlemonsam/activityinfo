package org.activityinfo.store.mysql.metadata;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;

/**
 * Stores information on activities that are linked to this activity
 */
public class LinkedActivity {
    
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
    
    public Multimap<Integer, Integer> getSourceMap() {
        Multimap<Integer, Integer> inverse = HashMultimap.create();
        Multimaps.invertFrom(linkMap, inverse);
        return inverse;
    }
}
