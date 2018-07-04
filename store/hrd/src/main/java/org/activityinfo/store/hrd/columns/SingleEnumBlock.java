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
import org.activityinfo.store.hrd.entity.FormColumnStorage;
import org.activityinfo.store.query.shared.columns.DiscreteStringColumnView;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SingleEnumBlock implements BlockManager {

    private static final String ITEM_ID_PROPERTY = "itemIds";
    private final EnumType enumType;

    public SingleEnumBlock(EnumType enumType) {
        this.enumType = enumType;
    }

    @Override
    public int getBlockSize() {
        return 10_000;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, FieldValue fieldValue) {

        String itemId = toItemId(fieldValue);

        char itemIndex = StringPools.findOrInsertStringInPool(blockEntity, ITEM_ID_PROPERTY, itemId);

        if(OffsetArray.updateOffset(blockEntity, recordOffset, itemIndex)) {
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
    public ColumnView buildView(FormColumnStorage header,
                                TombstoneIndex deleted, Iterator<Entity> blockIterator) {

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

        int[] values = new int[header.getRecordCount()];
        Arrays.fill(values, -1);

        while (blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            String[] pool = StringPools.toArray((Blob) block.getProperty(ITEM_ID_PROPERTY));
            if(pool.length > 0) {
                byte[] offsets = ((Blob)block.getProperty(OffsetArray.OFFSETS_PROPERTY)).getBytes();
                int offsetCount = OffsetArray.length(offsets);

                for (int i = 0; i < offsetCount; i++) {
                    int offset = OffsetArray.get(offsets, i);
                    if(offset != 0) {
                        String itemId = pool[offset - 1];
                        values[blockStart + i] = labelLookup.getInt(itemId);
                    }
                }
            }
        }
        return new DiscreteStringColumnView(ids, labels, values);
    }

}
