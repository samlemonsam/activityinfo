package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

/**
 * Legacy indicator metadata required to construct the pivot query
 */
public class IndicatorMetadata {
    int id;
    int activityId;
    String activityCategory;
    int aggregation;


    public int getId() {
        return id;
    }

    public int getActivityId() {
        return activityId;
    }

    public String getActivityCategory() {
        return activityCategory;
    }

    public int getAggregation() {
        return aggregation;
    }


    public ResourceId getFieldId() {
        return CuidAdapter.indicatorField(id);
    }
    
    public String getFieldExpression() {
        if(aggregation == 2) {
            return "1";
        } else {
            return CuidAdapter.indicatorField(id).asString();
        }
    }


    public String getAlias() {
        return "I" + id;
    }
}
