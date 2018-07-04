package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;

import javax.annotation.Nullable;
import java.util.Iterator;

public class RecordIdBlock implements BlockManager {

    public static final String FIELD_NAME = "$ID";

    public static final ResourceId FIELD_ID = ResourceId.valueOf(FIELD_NAME);

    @Override
    public int getBlockSize() {
        return 10_000;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue) {

        Blob strings = (Blob) blockEntity.getProperty("ids");
        int length = StringPools.size(strings);

        // since we only ever update when adding a new record, we should be able to simply append to the string pool
        if(recordOffset != length) {
            throw new IllegalStateException("length = " + length + ", recordOffset = " + recordOffset);
        }

        blockEntity.setProperty("ids", StringPools.appendString(strings, ((TextValue) fieldValue).asString()));

        return blockEntity;
    }

    @Override
    public ColumnView buildView(FormColumnStorage header, TombstoneIndex deleted, Iterator<Entity> blockIterator) {

        String[] ids = new String[header.getRecordCount()];

        while (blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            StringPools.toArray((Blob) block.getProperty("ids"), ids, blockStart);
        }

        return new StringArrayColumnView(ids);
    }
}
