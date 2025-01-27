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
package org.activityinfo.legacy.shared.command.result;

import java.io.Serializable;

public class SyncRegion implements Serializable {
    private String id;
    private String currentVersion;

    public SyncRegion() {
    }

    public SyncRegion(String id) {
        this.id = id;
    }

    public SyncRegion(String id, String currentVersion) {
        super();
        this.id = id;
        this.currentVersion = currentVersion;
    }

    public SyncRegion(String id, Object currentVersion) {
        this(id, currentVersion.toString());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncRegion that = (SyncRegion) o;

        if (currentVersion != null ? !currentVersion.equals(that.currentVersion) : that.currentVersion != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (currentVersion != null ? currentVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[" + id + ": " + currentVersion + "]";
    }
}
