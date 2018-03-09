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
