package org.activityinfo.store.query.impl.eval;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;

/**
 * Defines a join from a reference field to a linked FormClass
 */
public class JoinNode {
    
    private FormTree.Node referenceField;
    private ResourceId formClassId;

    public JoinNode(FormTree.Node referenceField, ResourceId formClassId) {
        this.referenceField = referenceField;
        this.formClassId = formClassId;
    }

    public FormTree.Node getReferenceField() {
        return referenceField;
    }

    public ResourceId getFormClassId() {
        return formClassId;
    }

    @Override
    public String toString() {
        return referenceField.getFieldId() + "->" + formClassId;
    }
}
