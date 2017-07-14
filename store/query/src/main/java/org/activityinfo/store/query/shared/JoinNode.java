package org.activityinfo.store.query.shared;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.resource.ResourceId;

/**
 * Defines a join from a reference field to a linked FormClass
 */
public class JoinNode {
    private JoinType type;
    private ResourceId leftFormId;
    private ExprNode referenceField;
    private ResourceId rightFormId;

    public JoinNode(JoinType type, ResourceId leftFormId, ExprNode referenceField, ResourceId rightFormId) {
        this.type = type;
        this.leftFormId = leftFormId;
        this.referenceField = referenceField;
        this.rightFormId = rightFormId;
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

        if (leftFormId != null ? !leftFormId.equals(joinNode.leftFormId) : joinNode.leftFormId != null) return false;
        if (referenceField != null ? !referenceField.equals(joinNode.referenceField) : joinNode.referenceField != null)
            return false;
        return rightFormId != null ? rightFormId.equals(joinNode.rightFormId) : joinNode.rightFormId == null;

    }

    @Override
    public int hashCode() {
        int result = leftFormId != null ? leftFormId.hashCode() : 0;
        result = 31 * result + (referenceField != null ? referenceField.hashCode() : 0);
        result = 31 * result + (rightFormId != null ? rightFormId.hashCode() : 0);
        return result;
    }
}
