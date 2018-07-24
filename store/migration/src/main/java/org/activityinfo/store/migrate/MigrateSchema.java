package org.activityinfo.store.migrate;

import com.google.appengine.tools.pipeline.ImmediateValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.io.Closeable;
import java.sql.SQLException;

public class MigrateSchema extends Job0<Void> {

    private ResourceId formId;

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

                    if (schemaEntity == null) {
                        Activity activity;
                        try {
                            ActivityLoader loader = new ActivityLoader(new MySqlQueryExecutor());
                            activity = loader.load(CuidAdapter.getLegacyIdFromCuid(formId));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        schemaEntity = new FormSchemaEntity(activity.getSerializedFormClass());
                        schemaEntity.setSchemaVersion(rootEntity.getSchemaVersion());

                        Hrd.ofy().save().entity(schemaEntity).now();
                    }

                }
            });
        }
        return new ImmediateValue<>(null);
    }
}
