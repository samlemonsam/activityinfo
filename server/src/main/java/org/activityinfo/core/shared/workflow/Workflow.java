package org.activityinfo.core.shared.workflow;

/**
 * Spike for user-defined workflows to be implemented shortly. In the mean
 * time we need a way to lock down LocationTypes
 */
public class Workflow {

    public static final String OPEN_WORKFLOW_ID = "open";

    public static final String CLOSED_WORKFLOW_ID = "closed";

    public static final String VALIDATED = "validated";

    public static final String REJECTED = "rejected";

    public static boolean isValidId(String workflowId) {
        return OPEN_WORKFLOW_ID.equals(workflowId) || CLOSED_WORKFLOW_ID.equals(workflowId);
    }
}
