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
}
