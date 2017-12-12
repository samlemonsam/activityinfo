package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.TextType;

public class RecordIdNode extends PlanNode {

    private ResourceId formId;

    public RecordIdNode(ResourceId formId) {
        this.formId = formId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    @Override
    public FieldType getFieldType() {
        return TextType.SIMPLE;
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitRecordId(this);
    }
}
