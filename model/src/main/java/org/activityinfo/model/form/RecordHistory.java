package org.activityinfo.model.form;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordHistory {

    private boolean available;
    private RecordHistoryEntry[] entries;

    public RecordHistory() {
    }

    @JsOverlay
    public static RecordHistory unavailable() {
        RecordHistory history = new RecordHistory();
        history.available = false;
        return history;
    }

    @JsOverlay
    public static RecordHistory create(List<RecordHistoryEntry> entries) {
        RecordHistory history = new RecordHistory();
        history.available = true;
        history.entries = entries.toArray(new RecordHistoryEntry[entries.size()]);
        return history;
    }

    /**
     *
     * @return true if history is available for this record. Not all form stores support history tracking.
     */
    @JsOverlay
    public boolean isAvailable() {
        return available;
    }

    @JsOverlay
    public List<RecordHistoryEntry> getEntries() {
        if(entries == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(entries);
    }
}
