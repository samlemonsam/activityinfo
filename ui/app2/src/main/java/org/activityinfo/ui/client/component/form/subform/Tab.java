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
package org.activityinfo.ui.client.component.form.subform;

import org.activityinfo.model.form.SubFormKind;

/**
 * A single tab, representing, for example 'May 2006'
 */
public class Tab {
    
    private String id;
    private String label;
    private SubFormKind kind;

    public Tab(String id, String label, SubFormKind kind) {
        this.id = id;
        this.label = label;
        this.kind = kind;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public SubFormKind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tab tab = (Tab) o;

        return !(id != null ? !id.equals(tab.id) : tab.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
