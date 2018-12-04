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
package org.activityinfo.server.job;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import org.activityinfo.io.csv.CsvWriter;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Writes an audit log
 */
public class AuditLogWriter {
    private final UserDatabaseDTO db;
    private CsvWriter csv;
    private EntityManager entityManager;

    private LoadingCache<Integer, User> userCache;

    public AuditLogWriter(final EntityManager entityManager, UserDatabaseDTO db, CsvWriter csv) throws IOException {
        this.entityManager = entityManager;
        this.csv = csv;
        this.db = db;

        writeHeaders();

        userCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<Integer, User>() {
                    @Override
                    public User load(Integer userId) throws Exception {
                        User user = entityManager.find(User.class, userId);
                        entityManager.detach(user);
                        return user;
                    }
                });
    }

    public void writeHeaders() throws IOException {
        csv.writeLine("Time", "Action", "User Email", "User Name", "Database ID", "Database Name",
                "Form ID", "Form Name", "Field ID", "Field Name", "Record ID", "Record Partner");
    }

    public void writeForm(FormStorageProvider catalog, ResourceId formId) throws IOException {

        FormStorage formStorage = catalog.getForm(formId).get();
        FormClass formClass = formStorage.getFormClass();

        Key<FormEntity> parentKey = FormEntity.key(formId);
        Query<FormRecordSnapshotEntity> query = Hrd.ofy().load().type(FormRecordSnapshotEntity.class).ancestor(parentKey);

        for (FormRecordSnapshotEntity snapshot : query) {

            User user;
            try {
                user = userCache.get((int)snapshot.getUserId());
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            csv.writeLine(
                    formatTime(snapshot.getTime()),
                    snapshot.getType().name(),
                    user.getEmail(),
                    user.getName(),
                    databaseId(),
                    db.getName(),
                    formId.asString(),
                    formClass.getLabel(),
                    "", // Field ID
                    "", // Field Name
                    snapshot.getRecordId().asString(),
                    partner()
            );
        }

    }

    private String databaseId() {
        return Integer.toString(db.getId());
    }

    private String partner() {
        return "Default";
    }

    private String formatTime(Date time) {
        return time.toString();
    }

    public String toString() {
        return csv.toString();
    }

}
