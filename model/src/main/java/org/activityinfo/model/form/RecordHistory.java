package org.activityinfo.model.form;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

import java.util.Arrays;
import java.util.List;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class RecordHistory {

    private RecordHistoryEntry[] entries;

    public RecordHistory() {
    }

    @JsOverlay
    public static RecordHistory create(List<RecordHistoryEntry> entries) {
        RecordHistory history = new RecordHistory();
        history.entries = entries.toArray(new RecordHistoryEntry[entries.size()]);
        return history;
    }

    @JsOverlay
    public List<RecordHistoryEntry> getEntries() {
        return Arrays.asList(entries);
    }
}
