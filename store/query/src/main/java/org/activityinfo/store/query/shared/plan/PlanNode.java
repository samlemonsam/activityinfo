package org.activityinfo.store.query.shared.plan;

import java.util.List;

public interface PlanNode {

    default String getDebugId() {
        return getClass().getSimpleName() + "_" + System.identityHashCode(this);
    }

    String getDebugLabel();

    List<? extends PlanNode> getInputs();

}
