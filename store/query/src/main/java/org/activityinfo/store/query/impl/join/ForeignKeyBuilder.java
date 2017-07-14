package org.activityinfo.store.query.impl.join;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.query.impl.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;

/**
 * Constructs a ForeignKey by listening for a cursor.
 *
 */
public class ForeignKeyBuilder implements CursorObserver<FieldValue> {


    /**
     * The form against which we are joining.
     *
     * <p>An ActivityInfo reference field can reference more than one form, but for this
     * data structure, we are only interesting in mapping each row to a unique row in a specific
     * referenced form.</p>
     */
    private final ResourceId rightFormId;

    private final PendingSlot<ForeignKey> result;

    /**
     * Map from key to key index.
     *
     * <p>Foreign keys are stored as strings and can be quite long. There does, however, tend to be alot
     * of repetition. To avoid using far more storage that we need, particularly when serializing to memcache,
     * we maintain a list of unique foreign keys and assign them a new integer id.</p>
     */
    private Object2IntOpenHashMap<String> keyMap = new Object2IntOpenHashMap<>();

    /**
     * Vector that contains an entry for each row, with the key index as value.
     */
    private IntArrayList keys = new IntArrayList();



    public ForeignKeyBuilder(ResourceId rightFormId, PendingSlot<ForeignKey> result) {
        this.result = result;
        this.rightFormId = rightFormId;
        keyMap.defaultReturnValue(-1);
    }

    @Override
    public void onNext(FieldValue fieldValue) {
        int count = 0;
        int key = -1;

        if(fieldValue instanceof ReferenceValue) {
            ReferenceValue referenceValue = (ReferenceValue) fieldValue;
            for (RecordRef id : referenceValue.getReferences()) {
                if(id.getFormId().equals(rightFormId)) {
                    count++;
                    key = keyId(id);
                }
            }
        }
        if(count == 1) {
            keys.add(key);
        } else {
            keys.add(-1);
        }
    }

    /**
     * Finds or creates an integer key id for this String key.
     */
    private int keyId(RecordRef id) {
        String stringKey = id.getRecordId().asString();
        int keyIndex = keyMap.getInt(stringKey);
        if(keyIndex == -1) {
            keyIndex = keyMap.size();
            keyMap.put(stringKey, keyIndex);
        }
        return keyIndex;
    }

    private String[] keyList() {
        String[] keys = new String[keyMap.size()];
        for (Object2IntMap.Entry<String> entry : keyMap.object2IntEntrySet()) {
            keys[entry.getIntValue()] = entry.getKey();
        }
        return keys;
    }

    @Override
    public void done() {
        result.set(build());
    }

    @VisibleForTesting
    ForeignKey build() {
        return new ForeignKey32(keyList(), keys);
    }

}
