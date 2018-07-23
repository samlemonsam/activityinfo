package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.query.shared.columns.DiscreteStringColumnView;

import java.util.*;

public class SingleEnumBlock implements BlockManager {

    private final EnumType enumType;
    private final String itemIdProperty;
    private final String offsetProperty;

    public SingleEnumBlock(String fieldName, EnumType enumType) {
        this.enumType = enumType;
        this.itemIdProperty = fieldName + ":pool";
        this.offsetProperty = fieldName;
    }

    @Override
    public int getBlockSize() {
        return 1024 * 4;
    }

    @Override
    public int getMaxFieldSize() {
        return 4;
    }

    @Override
    public String getBlockType() {
        return "enum";
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, FieldValue fieldValue) {

        String itemId = toItemId(fieldValue);

        char itemIndex = StringPools.findOrInsertStringInPool(blockEntity, itemIdProperty, itemId);

        if(OffsetArray.updateOffset(blockEntity, offsetProperty, recordOffset, itemIndex)) {
            return blockEntity;
        } else {
            /* no change */
            return null;
        }
    }

    private String toItemId(FieldValue fieldValue) {

        if(fieldValue instanceof EnumValue) {
            EnumValue enumValue = (EnumValue) fieldValue;
            Set<ResourceId> ids = enumValue.getResourceIds();
            Preconditions.checkState(ids.size() == 1, "ids = %s", ids.toString());

            return ids.iterator().next().asString();
        }
        return null;
    }

    @Override
    public ColumnView buildView(FormEntity header,
                                TombstoneIndex tombstones, Iterator<Entity> blockIterator, String component) {

        Object2IntOpenHashMap<String> labelLookup = new Object2IntOpenHashMap<>();
        labelLookup.defaultReturnValue(-1);

        String[] ids = new String[enumType.getValues().size()];
        String[] labels = new String[enumType.getValues().size()];
        List<EnumItem> items = enumType.getValues();
        for (int i = 0; i < items.size(); i++) {
            EnumItem enumItem = items.get(i);
            ids[i] = enumItem.getId().asString();
            labels[i] = enumItem.getLabel();
            labelLookup.put(enumItem.getId().asString(), i);
        }

        int[] values = new int[header.getNumberedRecordCount()];
        Arrays.fill(values, -1);

        while (blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            // Adjust start position depending on the number of records that have been deleted
            // in preceding blocks.
            int targetIndex = blockStart - tombstones.countDeletedBefore(blockStart);

            String[] pool = StringPools.toArray((Blob) block.getProperty(itemIdProperty));
            if(pool.length > 0) {
                byte[] offsets = ((Blob)block.getProperty(offsetProperty)).getBytes();
                int offsetCount = OffsetArray.length(offsets);

                BitSet deleted = tombstones.getDeletedBitSet(blockStart, getBlockSize());

                for (int i = 0; i < offsetCount; i++) {
                    if(!deleted.get(i)) {
                        int offset = OffsetArray.get(offsets, i);
                        if (offset != 0) {
                            String itemId = pool[offset - 1];
                            values[targetIndex] = labelLookup.getInt(itemId);
                        }
                        targetIndex++;
                    }
                }
            }
        }
        return new DiscreteStringColumnView(ids, labels, values);
    }

}
