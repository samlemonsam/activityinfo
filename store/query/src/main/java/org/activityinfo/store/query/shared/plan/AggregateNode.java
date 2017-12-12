package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.expr.functions.StatFunction;
import org.activityinfo.model.type.FieldType;

import java.util.Collections;

public class AggregateNode extends PlanNode {

    private StatFunction function;
    private PlanNode value;
    private FieldType type;

    public AggregateNode(StatFunction function, PlanNode value) {
        this.function = function;
        this.value = value;
        this.type = function.resolveResultType(Collections.singletonList(value.getFieldType()));
    }

    @Override
    public FieldType getFieldType() {
        return type;
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitAggregate(this);
    }
}
