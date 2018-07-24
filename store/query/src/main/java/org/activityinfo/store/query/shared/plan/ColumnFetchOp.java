package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.List;

/**
 * Fetches all values from a field
 */
public class ColumnFetchOp extends ColumnPlanNode {

    private ResourceId formId;
    private FormField field;

    public ColumnFetchOp(ResourceId formId, FormField field) {
        this.formId = formId;
        this.field = field;
    }

    @Override
    public String getDebugLabel() {
        return "fetch " + formId + "." + field.getName();
    }

    @Override
    public List<? extends PlanNode> getInputs() {
        return Collections.emptyList();
    }
}
