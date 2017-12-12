package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.primitive.BooleanType;

public class EnumNode extends PlanNode {

    private PlanNode fieldNode;
    private EnumItem enumItem;

    public EnumNode(PlanNode fieldNode, EnumItem enumItem) {
        this.fieldNode = fieldNode;
        this.enumItem = enumItem;
    }

    public PlanNode getFieldNode() {
        return fieldNode;
    }

    public EnumItem getEnumItem() {
        return enumItem;
    }

    @Override
    public FieldType getFieldType() {
        return BooleanType.INSTANCE;
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitEnumItem(this);
    }
}
