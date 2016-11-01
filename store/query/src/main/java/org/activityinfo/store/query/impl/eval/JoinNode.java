package org.activityinfo.store.query.impl.eval;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.resource.ResourceId;

/**
 * Defines a join from a reference field to a linked FormClass
 */
public class JoinNode {
    
    private ResourceId leftFormId;
    private ExprNode referenceField;
    private ResourceId formClassId;

    public JoinNode(ResourceId leftFormId, ExprNode referenceField, ResourceId rightFormId) {
        this.leftFormId = leftFormId;
        this.referenceField = referenceField;
        this.formClassId = rightFormId;
    }

    public ResourceId getLeftFormId() {
        return leftFormId;
    }

    public ExprNode getReferenceField() {
        return referenceField;
    }

    public ResourceId getFormClassId() {
        return formClassId;
    }

    @Override
    public String toString() {
        return referenceField + "->" + formClassId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinNode joinNode = (JoinNode) o;

        if (leftFormId != null ? !leftFormId.equals(joinNode.leftFormId) : joinNode.leftFormId != null) return false;
        if (referenceField != null ? !referenceField.equals(joinNode.referenceField) : joinNode.referenceField != null)
            return false;
        return formClassId != null ? formClassId.equals(joinNode.formClassId) : joinNode.formClassId == null;

    }

    @Override
    public int hashCode() {
        int result = leftFormId != null ? leftFormId.hashCode() : 0;
        result = 31 * result + (referenceField != null ? referenceField.hashCode() : 0);
        result = 31 * result + (formClassId != null ? formClassId.hashCode() : 0);
        return result;
    }
}
