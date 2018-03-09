/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        object.put("descriptor", descriptor.toJson());
        object.put("state", state.name().toLowerCase());
        if(result != null) {
            object.put("result", result.toJson());
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
