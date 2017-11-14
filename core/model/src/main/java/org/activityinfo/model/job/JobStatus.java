package org.activityinfo.model.job;

import org.activityinfo.json.JsonValue;

import static org.activityinfo.json.Json.createObject;

/**
 * Shows the status of the job status
 */
public class JobStatus<T extends JobDescriptor<R>, R extends JobResult> {

    private String id;
    private T descriptor;
    private JobState state;
    private R result;

    public JobStatus(String id, T descriptor, JobState state, R jobResult) {
        this.id = id;
        this.descriptor = descriptor;
        this.state = state;
        this.result = jobResult;
    }

    public String getId() {
        return id;
    }

    public T getDescriptor() {
        return descriptor;
    }

    public R getResult() {
        return result;
    }

    public JobState getState() {
        return state;
    }

    public JsonValue toJsonObject() {
        JsonValue object = createObject();
        object.put("id", id);
        object.put("type", descriptor.getType());
        object.put("descriptor", descriptor.toJsonObject());
        object.put("state", state.name().toLowerCase());
        if(result != null) {
            object.put("result", result.toJsonObject());
        }
        return object;
    }

    public static JobStatus fromJson(JsonValue object) {
        String id = object.get("id").asString();
        String type = object.get("type").asString();
        JobDescriptor descriptor = JobRequest.parseDescriptor(type, object.get("descriptor"));
        JobState state = JobState.valueOf(object.get("state").asString().toUpperCase());
        JobResult result = null;
        if(object.hasKey("result")) {
            result = descriptor.parseResult(object.get("result"));
        }

        return new JobStatus(id, descriptor, state, result);
    }

}
