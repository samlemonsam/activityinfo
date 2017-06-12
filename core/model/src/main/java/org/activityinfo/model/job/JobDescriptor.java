package org.activityinfo.model.job;

import com.google.gson.JsonObject;

/**
 * Defines a long-running job running on the server, performed
 * on behalf of a single user.
 */
public interface JobDescriptor<T extends JobResult> {

    /**
     * @return the job type id;
     */
    String getType();

    T parseResult(JsonObject resultObject);

    JsonObject toJsonObject();
}
