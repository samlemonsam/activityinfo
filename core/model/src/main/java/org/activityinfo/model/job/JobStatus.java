package org.activityinfo.model.job;

import com.google.gson.JsonObject;

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

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("type", descriptor.getType());
        object.add("descriptor", descriptor.toJsonObject());
        object.addProperty("state", state.name().toLowerCase());
        if(result != null) {
            object.add("result", result.toJsonObject());
        }
        return object;
    }

    public static JobStatus fromJson(JsonObject object) {
        String id = object.get("id").getAsString();
        String type = object.get("type").getAsString();
        JobDescriptor descriptor = JobRequest.parseDescriptor(type, object.get("descriptor").getAsJsonObject());
        JobState state = JobState.valueOf(object.get("state").getAsString().toUpperCase());
        JobResult result = null;
        if(object.has("result")) {
            result = descriptor.parseResult(object.get("result").getAsJsonObject());
        }

        return new JobStatus(id, descriptor, state, result);
    }

}
