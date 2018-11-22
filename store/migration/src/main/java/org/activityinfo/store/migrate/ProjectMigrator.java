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
package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.objectify.Work;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.mysql.collections.ProjectTable;
import org.activityinfo.store.mysql.cursor.RecordCursor;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.metadata.DatabaseCacheImpl;
import org.activityinfo.store.spi.RecordChangeType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Migrates database project forms to "normal" hrd-backed forms.
 */
public class ProjectMigrator extends MapOnlyMapper<Integer, Void> {

    private static final Logger LOGGER = Logger.getLogger(ProjectMigrator.class.getName());


    @Override
    public void map(final Integer databaseId) {

        try (MySqlQueryExecutor executor = new MySqlQueryExecutor()) {

            ProjectTable table = new ProjectTable(new DatabaseCacheImpl(executor));
            final TableMapping mapping = table.getMapping(executor, CuidAdapter.projectFormClass(databaseId));
            final ResourceId formId = mapping.getFormClass().getId();

            Hrd.run(new Work<Void>() {
                @Override
                public Void run() {
                    RecordCursor recordBuilder = new RecordCursor(mapping, executor);
                    Iterator<TypedFormRecord> it = recordBuilder.execute();

                    // Only create if there is one or more projects....
                    if(!it.hasNext()) {
                        return null;
                    }

                    final List<Object> toSave = new ArrayList<>();

                    FormEntity formEntity = new FormEntity();
                    formEntity.setId(formId);
                    formEntity.setSchemaVersion(1L);
                    formEntity.setVersion(mapping.getVersion());
                    toSave.add(formEntity);

                    FormSchemaEntity schemaEntity = new FormSchemaEntity(mapping.getFormClass());
                    schemaEntity.setSchemaVersion(1L);
                    schemaEntity.setSchema(mapping.getFormClass());
                    toSave.add(schemaEntity);


                    Date updateTime = new Date();
                    long userId = -1L;

                    while (it.hasNext()) {

                        TypedFormRecord record = it.next();

                        FormRecordEntity recordEntity = new FormRecordEntity(formId, record.getId());
                        recordEntity.setFieldValues(mapping.getFormClass(), record.getFieldValueMap());
                        recordEntity.setSchemaVersion(1L);
                        recordEntity.setVersion(1L);

                        FormRecordSnapshotEntity snapshot = new FormRecordSnapshotEntity(
                                userId, RecordChangeType.CREATED, recordEntity);
                        snapshot.setTime(updateTime);
                        snapshot.setMigrated(true);

                        toSave.add(record);
                        toSave.add(snapshot);
                    }

                    Hrd.ofy().save().entities(toSave).now();
                    return null;
                }
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
