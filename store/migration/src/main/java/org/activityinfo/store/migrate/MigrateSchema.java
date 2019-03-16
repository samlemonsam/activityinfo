package org.acivityinfo.store.migrate;

import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.migrate.HrdPrimary;
import org.activityinfo.store.migrate.MySqlQueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.io.Closeable;
import java.sql.SQLException;

/**
 * Step 1: Ensure that the latest version of the schema is stored in HRD
 */
public class MigrateSchema extends Job0<Void> {

    private ResourceId formId;

    public MigrateSchema(int activityId) {
        this.formId = CuidAdapter.activityFormClass(activityId);
    }

    public MigrateSchema(ResourceId formId) {
        this.formId = formId;
    }

    @Override
    public Value<Void> run() throws Exception {

        try(Closeable o = ObjectifyService.begin()) {

            Hrd.ofy().transact(new VoidWork() {
                @Override
                public void vrun() {

                    FormEntity rootEntity = Hrd.ofy().load().key(FormEntity.key(formId)).now();
                    FormSchemaEntity schemaEntity = Hrd.ofy().load().key(FormSchemaEntity.key(formId)).now();


                    if (schemaEntity == null || rootEntity == null) {
                        Activity activity;
                        try {
                            ActivityLoader loader = new ActivityLoader(new MySqlQueryExecutor());
                            activity = loader.load(CuidAdapter.getLegacyIdFromCuid(formId));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        if(schemaEntity == null) {
                            schemaEntity = new FormSchemaEntity(activity.getSerializedFormClass());
                            schemaEntity.setSchemaVersion(rootEntity.getSchemaVersion());
                            Hrd.ofy().save().entity(schemaEntity).now();
                        }
                        if(rootEntity == null) {
                            rootEntity = new FormEntity();
                            rootEntity.setSchemaVersion(schemaEntity.getSchemaVersion());
                            rootEntity.setId(activity.getSiteFormClassId());
                            rootEntity.setVersion(activity.getVersion());
                            Hrd.ofy().save().entity(rootEntity).now();
                        }

                    }

                }
            });
        }
        return futureCall(new HrdPrimary(CuidAdapter.getLegacyIdFromCuid(formId)));
    }
}
