package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;

public class ReferencePlanNode extends PlanNode {

    /**
     * The id of the form where the reference field is declared
     */
    private ResourceId leftFormId;

    /**
     * The id of the reference field
     */
    private FormField leftField;

    /**
     * The right form id to be joined.
     */
    private ResourceId rightFormId;

    /**
     * The value on the right form that we are joining
     */
    private PlanNode rightNode;


    public ReferencePlanNode(ResourceId leftFormId, FormField leftField, ResourceId rightFormId, PlanNode rightNode) {
        this.leftFormId = leftFormId;
        this.leftField = leftField;
        this.rightFormId = rightFormId;
        this.rightNode = rightNode;
    }

    public ResourceId getLeftFormId() {
        return leftFormId;
    }

    public FormField getLeftField() {
        return leftField;
    }

    public String getReferenceField() {
        return leftField.getName();
    }

    public ResourceId getRightFormId() {
        return rightFormId;
    }

    public PlanNode getRightNode() {
        return rightNode;
    }

    @Override
    public FieldType getFieldType() {
        return rightNode.getFieldType();
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitReference(this);
    }

}
