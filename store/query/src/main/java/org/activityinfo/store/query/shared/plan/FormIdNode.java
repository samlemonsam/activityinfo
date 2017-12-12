package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.TextType;

public class FormIdNode extends PlanNode {
    @Override
    public FieldType getFieldType() {
        return TextType.SIMPLE;
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitFormId(this);
    }
}
