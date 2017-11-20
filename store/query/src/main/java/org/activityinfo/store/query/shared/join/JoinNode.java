package org.activityinfo.store.query.shared.join;

import com.google.common.base.Optional;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.functions.StatFunction;
import org.activityinfo.model.resource.ResourceId;

/**
 * Defines a join from a reference field to a linked FormClass
 */
public class JoinNode {
    private JoinType type;
    private ResourceId leftFormId;
    private ExprNode referenceField;
    private ResourceId rightFormId;
    private Optional<StatFunction> aggregation;

    public JoinNode(JoinType type, ResourceId leftFormId, ExprNode referenceField, ResourceId rightFormId,
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

    public JoinNode(JoinType type, ResourceId leftFormId, ExprNode referenceField, ResourceId rightFormId) {
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

    public ExprNode getReferenceField() {
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
