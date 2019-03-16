package org.activityinfo.store.migrate;

import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Value;
import com.googlecode.objectify.ObjectifyService;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.mapping.TableMigrator;

import java.io.Closeable;

public class MigrateFormJob extends Job1<Void, String> {

    @Override
    public Value<Void> run(String formIdString) throws Exception {
        try(Closeable objectify = ObjectifyService.begin()) {
            TableMigrator.migrate(ResourceId.valueOf(formIdString), new MySqlQueryExecutor());
        }
        return immediate(null);
    }

}
