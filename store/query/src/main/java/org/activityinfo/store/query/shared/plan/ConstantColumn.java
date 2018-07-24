package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.type.FieldValue;

import java.util.Collections;
import java.util.List;

public class ConstantColumn extends ColumnPlanNode {

    private final FieldValue value;
    private CountRecordsOp countRecords;

    public ConstantColumn(FieldValue constantValue, CountRecordsOp countRecords) {
        this.value = constantValue;
        this.countRecords = countRecords;
    }

    @Override
    public String getDebugLabel() {
        return "constant";
    }

    @Override
    public List<? extends PlanNode> getInputs() {
        return Collections.singletonList(countRecords);
    }
}
