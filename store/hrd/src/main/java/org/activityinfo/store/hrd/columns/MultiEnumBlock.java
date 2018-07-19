package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.hrd.entity.FormEntity;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MultiEnumBlock implements BlockManager {
    @Override
    public int getBlockSize() {
        return 1024 * 5;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue) {

        Set<String> toUnset = new HashSet<>(blockEntity.getProperties().keySet());

        boolean dirty = false;

        if(fieldValue instanceof EnumValue) {
            EnumValue enumValue = (EnumValue) fieldValue;
            for (ResourceId enumId : enumValue.getResourceIds()) {
                String propertyKey = "v:" + enumId.asString();
                if (BlobBitSet.update(blockEntity, propertyKey, recordOffset, true)) {
                    dirty = true;
                }
                toUnset.remove(propertyKey);
            }
        }

        // Unset any existing bit vectors
        for (String property : toUnset) {
            if (BlobBitSet.update(blockEntity, property, recordOffset, false)) {
                dirty = true;
            }
        }

        if(dirty) {
            return blockEntity;
        } else {
            return null;
        }
    }

    @Override
    public ColumnView buildView(FormEntity header, TombstoneIndex deleted, Iterator<Entity> blockIterator) {
        throw new UnsupportedOperationException("TODO");
    }
}
