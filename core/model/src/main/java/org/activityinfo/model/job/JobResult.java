package org.activityinfo.model.job;

import com.google.gson.JsonObject;

/**
 * Describes the results of a successfully-completed job
 */
public interface JobResult {

    JsonObject toJsonObject();
}
