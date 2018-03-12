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
package org.activityinfo.server.digest.geo;

import com.google.common.base.Strings;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.reports.content.MapContent;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.digest.DigestModel;
import org.activityinfo.server.digest.UserDigest;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class GeoDigestModel implements DigestModel {

    private final UserDigest userDigest;
    private SchemaDTO schemaDTO;
    private final Set<DatabaseModel> databases;

    public GeoDigestModel(UserDigest userDigest) {
        this.userDigest = userDigest;
        this.databases = new TreeSet<>();
    }

    public UserDigest getUserDigest() {
        return userDigest;
    }

    public SchemaDTO getSchemaDTO() {
        return schemaDTO;
    }

    public void setSchemaDTO(SchemaDTO schemaDTO) {
        this.schemaDTO = schemaDTO;
    }

    @Override
    public boolean hasData() {
        for (DatabaseModel db : databases) {
            if (!db.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void addDatabase(DatabaseModel databaseModel) {
        databases.add(databaseModel);
    }

    public Collection<DatabaseModel> getDatabases() {
        return databases;
    }

    public static class DatabaseModel implements Comparable<DatabaseModel> {
        private final GeoDigestModel model;
        private final Database database;
        private MapContent content;
        private String url;

        public DatabaseModel(GeoDigestModel model, Database database) {
            this.model = model;
            this.database = database;

            model.addDatabase(this);
        }

        public GeoDigestModel getModel() {
            return model;
        }

        public Database getDatabase() {
            return database;
        }

        public String getName() {
            return database.getName();
        }

        public MapContent getContent() {
            return content;
        }

        public void setContent(MapContent content) {
            this.content = content;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isRenderable() {
            return content != null && !content.getMarkers().isEmpty() && !Strings.isNullOrEmpty(url);
        }

        public boolean isEmpty() {
            return !isRenderable();
        }

        @Override
        public int compareTo(DatabaseModel o) {
            return database.getName().compareTo(o.database.getName());
        }
    }
}
