package org.activityinfo.server.command.handler.pivot;

import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
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

    public ResourceId getTargetFieldId() {
        return CuidAdapter.cuid(CuidAdapter.TARGET_INDICATOR_FIELD_DOMAIN, sourceId);
    }

    public ExprNode getFieldExpression() {
        switch (aggregation) {
            case IndicatorDTO.AGGREGATE_SITE_COUNT:
                return new ConstantExpr(1);
            default:
                return new SymbolExpr(CuidAdapter.indicatorField(sourceId));
        }
    }
    
    public ExprNode getTargetFieldExpression() {
        return new SymbolExpr(CuidAdapter.indicatorField(sourceId));
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
