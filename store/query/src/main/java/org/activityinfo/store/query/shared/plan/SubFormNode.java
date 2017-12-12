package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;

public class SubFormNode extends PlanNode {

    private ResourceId parentFormId;
    private FormField subFormField;
    private PlanNode subFormNode;

    public SubFormNode(ResourceId parentFormId, FormField subFormField, PlanNode subFormNode) {
        this.parentFormId = parentFormId;
        this.subFormField = subFormField;
        this.subFormNode = subFormNode;
    }

    @Override
    public FieldType getFieldType() {
        return subFormNode.getFieldType();
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitSubForm(this);
    }
}
