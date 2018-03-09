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
package org.activityinfo.ui.client.component.form.field.hierarchy;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

/**
 * 
 */
public class Choice {
    private RecordRef ref;
    private String label;
    private RecordRef parentRef;

    public Choice(ResourceId formId, ResourceId id, String label) {
        this.label = label;
        this.ref = new RecordRef(formId, id);
    }

    public Choice(ResourceId formId, ResourceId id, String label, RecordRef parentRef) {
        this.label = label;
        this.ref = new RecordRef(formId, id);
        this.parentRef = parentRef;
    }

    public RecordRef getRef() {
        return ref;
    }

    public String getLabel() {
        return label;
    }

    public RecordRef getParentRef() {
        assert parentRef != null;
        return parentRef;
    }

    public boolean hasParent() {
        return parentRef != null;
    }
}
