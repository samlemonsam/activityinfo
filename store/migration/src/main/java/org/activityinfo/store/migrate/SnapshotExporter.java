package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.googlecode.objectify.Work;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.HrdStorageProvider;
import org.activityinfo.store.spi.FormStorage;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SnapshotExporter extends MapOnlyMapper<Entity, ByteBuffer> {

    static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

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
        line.append(TIME_FORMAT.print(time.getTime()));
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
            databaseId = fetchFromHrd(formId);
            if(databaseId == null && formId.startsWith("a")) {
                databaseId = fetchFromMySqlFormId(formId);
            }
            databaseCache.put(formId, databaseId);
        }
        return databaseId;
    }

    private String fetchFromHrd(String formId) {
        String databaseId;
        databaseId = Hrd.run(new Work<String>() {
            @Override
            public String run() {
                HrdStorageProvider provider = new HrdStorageProvider();
                Optional<FormStorage> storage = provider.getForm(ResourceId.valueOf(formId));
                return storage.transform(s -> s.getFormClass().getDatabaseId().asString()).orNull();
            }
        });
        return databaseId;
    }

    private String fetchFromMySqlFormId(String formId) {
        int activityId;
        try {
            activityId = CuidAdapter.getLegacyIdFromCuid(formId);
        } catch (Exception e) {
            return null;
        }
        try (MySqlQueryExecutor executor = new MySqlQueryExecutor()) {
            try (ResultSet resultSet = executor.query("SELECT databaseId FROM activity WHERE activityId = ?", activityId)) {
                if (resultSet.next()) {
                    return CuidAdapter.databaseId(resultSet.getInt(1)).asString();
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
