package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.googlecode.objectify.Work;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.HrdStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SnapshotExporter extends MapOnlyMapper<Entity, ByteBuffer> {

    static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private transient Map<String, String> databaseCache;

    @Override
    public void beginSlice() {
        super.beginSlice();
        databaseCache = new HashMap<>();
    }

    @Override
    public void map(Entity value) {
          // from_unixtime(timecreated/1000),userid,databaseId,formId,action,versionId

        Date time = (Date) value.getProperty("time");
        long userId = ((Number) value.getProperty("userId")).longValue();

        Key snapshotKey = value.getKey();
        Key recordKey = snapshotKey.getParent();
        Key formKey = recordKey.getParent();

        String formId = formKey.getName();
        String databaseId = lookupDatabaseId(formId);

        StringBuilder line = new StringBuilder();
        line.append(TIME_FORMAT.format(time));
        line.append(',');
        line.append(userId);
        line.append(',');
        line.append(databaseId);
        line.append(',');
        line.append(formId);
        line.append(",update_record,0\n");

        emit(ByteBuffer.wrap(line.toString().getBytes(Charsets.UTF_8)));
    }

    private String lookupDatabaseId(String formId) {
        String databaseId = databaseCache.get(formId);
        if(databaseId == null) {
            databaseId = Hrd.run(new Work<String>() {
                @Override
                public String run() {
                    HrdStorageProvider provider = new HrdStorageProvider();
                    Optional<FormStorage> storage = provider.getForm(ResourceId.valueOf(formId));
                    return storage.transform(s -> s.getFormClass().getDatabaseId().asString()).orNull();
                }
            });
            databaseCache.put(formId, databaseId);
        }
        return databaseId;
    }
}
