/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
