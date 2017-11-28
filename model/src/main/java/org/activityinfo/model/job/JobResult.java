package org.activityinfo.model.job;

import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

/**
 * Describes the results of a successfully-completed job
 */
public interface JobResult extends JsonSerializable {

    JsonValue toJson();
}
