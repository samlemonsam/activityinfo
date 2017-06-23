package org.activityinfo.model.job;

import org.activityinfo.json.JsonObject;

/**
 * Describes the results of a successfully-completed job
 */
public interface JobResult {

    JsonObject toJsonObject();
}
