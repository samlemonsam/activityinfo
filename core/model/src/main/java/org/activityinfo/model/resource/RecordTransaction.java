package org.activityinfo.model.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

import java.util.Arrays;

/**
 * Describes one or more changes to records.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordTransaction {

    String id;

    @JsonProperty(required = true)
    RecordUpdate[] changes;

    public RecordTransaction() {
    }

    @JsOverlay
    public Iterable<RecordUpdate> getChanges() {
        return Arrays.asList(changes);
    }
}
