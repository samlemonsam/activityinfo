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
package org.activityinfo.store.query.shared.join;

import com.google.common.base.Optional;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.functions.StatFunction;
import org.activityinfo.model.resource.ResourceId;

/**
 * Defines a join from a reference field to a linked FormClass
 */
public class JoinNode {
    private JoinType type;
    private ResourceId leftFormId;
    private FormulaNode referenceField;
    private ResourceId rightFormId;
    private Optional<StatFunction> aggregation;

    public JoinNode(JoinType type, ResourceId leftFormId, FormulaNode referenceField, ResourceId rightFormId,
                    Optional<StatFunction> aggregation) {
        this.type = type;
        this.leftFormId = leftFormId;
        this.referenceField = referenceField;
        this.rightFormId = rightFormId;
        if(this.type == JoinType.SUBFORM) {
            this.aggregation = aggregation;
        } else {
            this.aggregation = Optional.absent();
        }
    }

    public JoinNode(JoinType type, ResourceId leftFormId, FormulaNode referenceField, ResourceId rightFormId) {
        this(type, leftFormId, referenceField, rightFormId, Optional.<StatFunction>absent());
    }

    public Optional<StatFunction> getAggregation() {
        return aggregation;
    }

    public JoinType getType() {
        return type;
    }

    public ResourceId getLeftFormId() {
        return leftFormId;
    }

    public FormulaNode getReferenceField() {
        return referenceField;
    }

    public ResourceId getRightFormId() {
        return rightFormId;
    }

    @Override
    public String toString() {
        return referenceField + "->" + rightFormId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinNode joinNode = (JoinNode) o;

        if (type != joinNode.type) return false;
        if (!leftFormId.equals(joinNode.leftFormId)) return false;
        if (!referenceField.equals(joinNode.referenceField)) return false;
        if (!rightFormId.equals(joinNode.rightFormId)) return false;
        return aggregation.equals(joinNode.aggregation);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + leftFormId.hashCode();
        result = 31 * result + referenceField.hashCode();
        result = 31 * result + rightFormId.hashCode();
        result = 31 * result + aggregation.hashCode();
        return result;
    }
}
