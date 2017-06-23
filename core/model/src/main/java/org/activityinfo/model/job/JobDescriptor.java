package org.activityinfo.model.job;

import org.activityinfo.json.JsonObject;

/**
 * Defines a long-running job running on the server, performed
 * on behalf of a single user.
 */
public interface JobDescriptor<T extends JobResult> {

    /**
     * @return the job type id;
     */
    String getType();

    T parseResult(org.activityinfo.json.JsonObject resultObject);

    JsonObject toJsonObject();
}
