package org.activityinfo.server.command.handler.pivot;

import com.google.common.collect.Sets;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.Set;

/**
 * Legacy indicator metadata required to construct the pivot query
 */
public class IndicatorMetadata {
    String name;
    int sourceId;
    int destinationId;
    int aggregation;
    int sortOrder;
    Set<Integer> destinationIndicatorIds = Sets.newHashSet();

    public int getSourceId() {
        return sourceId;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public int getAggregation() {
        return aggregation;
    }

    public ResourceId getFieldId() {
        return CuidAdapter.indicatorField(sourceId);
    }
    
    public String getFieldExpression() {
        if(aggregation == 2) {
            return "1";
        } else {
            return CuidAdapter.indicatorField(sourceId).asString();
        }
    }


    public String getAlias() {
        return "I" + sourceId;
    }

    public String getName() {
        return name;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
