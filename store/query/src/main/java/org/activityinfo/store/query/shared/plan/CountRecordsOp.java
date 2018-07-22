package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.List;

/**
 * Counts the number of records in a form.
 */
public class CountRecordsOp implements PlanNode {

    private ResourceId formId;

    public CountRecordsOp(ResourceId formId) {
        this.formId = formId;
    }

    @Override
    public String getDebugLabel() {
        return "count " + formId;
    }

    @Override
    public List<? extends PlanNode> getInputs() {
        return Collections.emptyList();
    }
}
