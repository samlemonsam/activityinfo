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

import com.google.appengine.api.datastore.QueryResultIterable;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.hrd.op.CreateOrUpdateForm;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class FixSubForm extends HttpServlet {

    private static final boolean DRY_RUN = false;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final PrintWriter logger = new PrintWriter(resp.getOutputStream());
        resp.setContentType("text/plain");


        try(MySqlQueryExecutor executor = new MySqlQueryExecutor()) {
            try(ResultSet rs = executor.query(
                "select distinct i.activityid from indicator i " +
                    "left join activity a on (a.activityid=i.activityid) " +
                    "left join userdatabase db on (a.databaseid=db.databaseid) " +
                    "where type='subform' and " +
                    "i.datedeleted is null and " +
                    "a.datedeleted is null and " +
                    "db.datedeleted is null")) {
                while(rs.next()) {
                    final ResourceId parentFormId = CuidAdapter.activityFormClass(rs.getInt(1));

                    if (maybeFixForm(logger, executor, parentFormId)) {
                        return;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace(logger);
        } finally {
            logger.flush();
        }

    }

    private boolean maybeFixForm(final PrintWriter logger, MySqlQueryExecutor executor, ResourceId parentFormId) {
        logger.println("Fixing " + parentFormId + "...");

        final MySqlStorageProvider catalog = new MySqlStorageProvider(executor);
        FormStorage parentForm = catalog.getForm(parentFormId).get();
        final FormClass formClass = parentForm.getFormClass();

        logger.println("Loaded activity " + parentForm.getFormClass().getLabel());

        final List<FormField> updated = new ArrayList<>();

        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {
                Hrd.ofy().transact(new VoidWork() {
                    @Override
                    public void vrun() {

                        for (FormField formField : formClass.getFields()) {
                            if (formField.getType() instanceof SubFormReferenceType) {

                                if(maybeFixForm(logger, formClass, formField)) {
                                    updated.add(formField);
                                }
                            }
                        }
                    }
                });

            }
        });
        logger.println("TX COMPLETED!");

        if(!updated.isEmpty()) {
            logger.println("Updating parent form schema...");
            catalog.createOrUpdateFormSchema(formClass);
            return true;
        } else {
            return false;
        }
    }

    private boolean maybeFixForm(PrintWriter logger, FormClass parentForm, FormField formField) {
        SubFormReferenceType type = (SubFormReferenceType) formField.getType();
        ResourceId subFormId = type.getClassId();
        logger.println("Found subform " + formField.getLabel() + "(" + subFormId + ")");

        ResourceId parentFormId = parentForm.getId();

        FormSchemaEntity schemaEntity = Hrd.ofy().load().key(FormSchemaEntity.key(subFormId)).now();
        if(schemaEntity == null) {
            logger.println("Subform " + subFormId + " does not exist!!");
            return false;
        }
        FormClass schema = schemaEntity.readFormClass();

        if(schema.getParentFormId().get().equals(parentFormId)) {
            logger.println("Parent is OK");
            return false;
        }

        FormEntity root = Hrd.ofy().load().key(FormEntity.key(subFormId)).now();
        logger.println("At version " + root.getVersion());

        // Generate new subform id..
        ResourceId newSubFormId = ResourceId.generateId();
        schema.setId(newSubFormId);
        schema.setParentFormId(parentFormId);

        logger.println("Creating copy of subform => "  + newSubFormId);

        if (!DRY_RUN) {
            new CreateOrUpdateForm(schema).run();
        }

        // Now find all records and update their schema
        copyRecords(logger, parentFormId, subFormId, newSubFormId);
        copySnapshots(logger, parentFormId, subFormId, newSubFormId);

        // Update the root entity to match version numbers
        FormEntity newRoot = new FormEntity();
        newRoot.setId(newSubFormId);
        newRoot.setVersion(root.getVersion());
        newRoot.setSchemaVersion(root.getSchemaVersion());
        if(!DRY_RUN) {
            Hrd.ofy().save().entity(newRoot);
        }

        // Finally update the MySQL parent
        formField.setType(new SubFormReferenceType(newSubFormId));

        return true;
    }


    private void copyRecords(PrintWriter logger, ResourceId parentFormId, ResourceId subFormId, ResourceId newSubFormId) {
        QueryResultIterable<FormRecordEntity> subRecords = ofy().load()
            .type(FormRecordEntity.class)
            .ancestor(FormEntity.key(subFormId))
            .iterable();

        for (FormRecordEntity subRecord : subRecords) {
            ResourceId parentRecordId = ResourceId.valueOf(subRecord.getParentRecordId());
            if(isSubRecordOf(parentFormId, parentRecordId)) {
                logger.println("sub record " + subRecord.getRecordId() + " belongs to " + parentRecordId + " in " + parentFormId);
                copyTo(subRecord, newSubFormId);
            } else {
                logger.println("sub record " + subRecord.getRecordId() + " does not belong");
            }
        }
    }

    private void copySnapshots(PrintWriter logger, ResourceId parentFormId, ResourceId subFormId, ResourceId newSubFormId) {
        QueryResultIterable<FormRecordSnapshotEntity> subRecords = ofy().load()
            .type(FormRecordSnapshotEntity.class)
            .ancestor(FormEntity.key(subFormId))
            .iterable();

        for (FormRecordSnapshotEntity subRecord : subRecords) {
            ResourceId parentRecordId = ResourceId.valueOf(subRecord.getParentRecordId());
            String name = "sub record snapshot " + subRecord.getRecordId() + "@" + subRecord.getVersion();
            if(isSubRecordOf(parentFormId, parentRecordId)) {
                logger.println(name + " belongs to " + parentRecordId + " in " + parentFormId);
                copySnapshot(subRecord, newSubFormId);
            } else {
                logger.println(name + " does not belong");
            }
        }
    }


    private boolean isSubRecordOf(ResourceId parentFormId, ResourceId parentRecordId) {
        Key<FormRecordEntity> key = FormRecordEntity.key(parentFormId, parentRecordId);
        FormRecordEntity entity = Hrd.ofy().load().key(key).now();
        return entity != null;
    }

    private void copyTo(FormRecordEntity subRecord, ResourceId newSubFormId) {
        FormRecordEntity reparented = reparent(subRecord, newSubFormId);
        if(!DRY_RUN) {
            Hrd.ofy().save().entity(reparented);
        }
    }

    private FormRecordEntity reparent(FormRecordEntity subRecord, ResourceId newSubFormId) {
        FormRecordEntity newEntity = new FormRecordEntity(newSubFormId, subRecord.getRecordId());
        newEntity.setParentRecordId(subRecord.getParentRecordId());
        newEntity.setFieldValues(subRecord.getFieldValues());
        newEntity.setVersion(subRecord.getVersion());
        newEntity.setSchemaVersion(1);
        return newEntity;
    }

    private void copySnapshot(FormRecordSnapshotEntity old, ResourceId newSubFormId) {

        FormRecordSnapshotEntity newEntity = new FormRecordSnapshotEntity(
            old.getUserId(),
            old.getType(),
            reparent(old.getRecord(), newSubFormId));

        newEntity.setTime(old.getTime());
        newEntity.setParentRecordId(old.getParentRecordId());
        if(!DRY_RUN) {
            Hrd.ofy().save().entities(newEntity);
        }
    }
}
