package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.List;

public class IdFetchOp extends ColumnPlanNode {

    private ResourceId formId;

    public IdFetchOp(ResourceId formId) {
        this.formId = formId;
    }

    @Override
    public String getDebugLabel() {
        return "fetch " + formId;
    }

    @Override
    public List<? extends PlanNode> getInputs() {
        return Collections.emptyList();
    }
}
