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
package org.activityinfo.store.mysql.metadata;

public class ActivityVersion {
    private int id;
    private long schemaVersion;
    private long siteVersion;

    public ActivityVersion(int id, long schemaVersion, long siteVersion) {
        this.id = id;
        this.schemaVersion = schemaVersion;
        this.siteVersion = siteVersion;
    }

    public String getSchemaCacheKey() {
        return "activity:9:metadata:" + id + "@" + schemaVersion;
    }

    public int getId() {
        return id;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public long getSiteVersion() {
        return siteVersion;
    }
}
