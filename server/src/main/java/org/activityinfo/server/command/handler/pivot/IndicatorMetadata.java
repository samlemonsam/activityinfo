package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

/**
 * Legacy indicator metadata required to construct the pivot query
 */
public class IndicatorMetadata {
    String name;
    int sourceId;
    int destinationId;
    int aggregation;
    int sortOrder;

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

    public FormulaNode getFieldExpression() {
        switch (aggregation) {
            case IndicatorDTO.AGGREGATE_SITE_COUNT:
                return new ConstantNode(1);
            default:
                return new SymbolNode(CuidAdapter.indicatorField(sourceId));
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
