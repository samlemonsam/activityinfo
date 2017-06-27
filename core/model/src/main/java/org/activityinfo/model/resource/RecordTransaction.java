package org.activityinfo.model.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public String getId() {
        return id;
    }

    @JsOverlay
    public Iterable<RecordUpdate> getChanges() {
        return Arrays.asList(changes);
    }

    @JsOverlay
    public RecordUpdate[] getChangeArray() {
        return changes;
    }

    @JsOverlay
    public static RecordTransactionBuilder builder() {
        return new RecordTransactionBuilder();
    }

    @JsOverlay
    public Set<ResourceId> getAffectedFormIds() {
        Set<ResourceId> forms = new HashSet<>();
        for (RecordUpdate update : getChanges()) {
            forms.add(update.getFormId());
        }
        return forms;
    }
}
