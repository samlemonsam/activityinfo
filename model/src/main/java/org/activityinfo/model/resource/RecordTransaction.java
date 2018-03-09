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
