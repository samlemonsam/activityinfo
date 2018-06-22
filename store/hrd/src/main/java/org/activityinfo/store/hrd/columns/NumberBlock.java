package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterator;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EmptyColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormColumnStorage;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.activityinfo.store.query.shared.columns.IntReader;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NumberBlock implements BlockManager {


    interface IntColumnFactory {
        ColumnView create(int[] values);
    }

    private static final IntColumnFactory DEFAULT_COLUMN_BUILDER = IntValueArrayView::new;

    private static final int NO_STORAGE = 0;
    private static final int INT32_STORAGE = 1;
    private static final int REAL64_STORAGE = 2;

    private DoubleReader doubleReader;
    private IntReader intReader;
    private IntColumnFactory intColumnFactory;

    public NumberBlock(DoubleReader doubleReader) {
        this.doubleReader = doubleReader;
        this.intReader = null;
        this.intColumnFactory = DEFAULT_COLUMN_BUILDER;
    }

    public NumberBlock(IntReader intReader) {
        this.intReader = intReader;
        this.intColumnFactory = DEFAULT_COLUMN_BUILDER;
        this.doubleReader = null;
    }

    public NumberBlock(IntReader intReader, IntColumnFactory intColumnFactory) {
        this.intReader = intReader;
        this.intColumnFactory = intColumnFactory;
        this.doubleReader = null;

    }

    @Override
    public int getBlockSize() {
        return 10_000;
    }

    @Override
    public Entity update(Entity blockEntity, int recordOffset, FieldValue fieldValue) {

        if(fieldValue == null) {
            return updateMissing(blockEntity, recordOffset);

        } else if(intReader != null) {
            return updateInt(blockEntity, recordOffset, intReader.read(fieldValue));

        } else {
            double doubleValue = doubleReader.read(fieldValue);
            int storage = getStorageMode(blockEntity);

            if(storage == REAL64_STORAGE) {
                return updateDouble(blockEntity, recordOffset, doubleValue);
            } else {
                if(IntValueArray.accepts(doubleValue)) {
                    return updateInt(blockEntity, recordOffset, IntValueArray.toInt(doubleValue));
                } else {
                    if(storage == NO_STORAGE) {
                        return updateDouble(blockEntity, recordOffset, doubleValue);
                    } else {
                        return migrateToDoubleAndUpdate(blockEntity, recordOffset, doubleValue);
                    }
                }
            }
        }
    }


    private Entity updateMissing(Entity blockEntity, int recordOffset) {
        int mode = getStorageMode(blockEntity);
        switch (mode) {
            case NO_STORAGE:
                /* No change */
                return null;

            case INT32_STORAGE:
                return updateIntMissing(blockEntity, recordOffset);

            case REAL64_STORAGE:
                return updateDoubleMissing(blockEntity, recordOffset);
        }
        throw new UnsupportedOperationException("storage mode: " + mode);
    }


    private Entity updateIntMissing(Entity blockEntity, int recordOffset) {
        Blob valueArray = (Blob) blockEntity.getProperty("intValues");
        int currentLength = ValueArrays.length(valueArray, IntValueArray.BYTES);
        if(recordOffset < currentLength) {
            valueArray = IntValueArray.update(valueArray, recordOffset, IntValueArray.MISSING);
            blockEntity.setProperty("intValues", valueArray);
            return blockEntity;

        } else {
            /* No change */
            return null;
        }
    }

    private Entity updateInt(Entity blockEntity, int recordOffset, int read) {
        Blob valueArray = (Blob) blockEntity.getProperty("intValues");
        valueArray = IntValueArray.update(valueArray, recordOffset, read);

        blockEntity.setUnindexedProperty("storage", INT32_STORAGE);
        blockEntity.setProperty("intValues", valueArray);
        return blockEntity;
    }

    private Entity updateDoubleMissing(Entity blockEntity, int recordOffset) {
        Blob valueArray = (Blob) blockEntity.getProperty("doubleValues");
        int currentLength = ValueArrays.length(valueArray, ValueArrays.REAL64);
        if(recordOffset < currentLength) {
            valueArray = ValueArrays.updateReal64(valueArray, recordOffset, Double.NaN);
            blockEntity.setProperty("doubleValues", valueArray);
            return blockEntity;

        } else {
            /* No change */
            return null;
        }
    }

    private Entity updateDouble(Entity blockEntity, int recordOffset, double doubleValue) {
        Blob valueArray = (Blob) blockEntity.getProperty("doubleValues");
        valueArray = ValueArrays.updateReal64(valueArray, recordOffset, doubleValue);

        blockEntity.setUnindexedProperty("storage", REAL64_STORAGE);
        blockEntity.setProperty("doubleValues", valueArray);

        return blockEntity;
    }


    private Entity migrateToDoubleAndUpdate(Entity blockEntity, int recordOffset, double doubleValue) {
        Blob valueArray = (Blob) blockEntity.getProperty("doubleValues");

        int previousLength = IntValueArray.length(valueArray);
        int length = Math.max(previousLength, recordOffset + 1);

        byte[] updated = ValueArrays.allocate(length, ValueArrays.REAL64);

        ByteBuffer source = ValueArrays.asBuffer(valueArray);
        ByteBuffer target = ValueArrays.asBuffer(updated);

        for (int i = 0; i < length; i++) {
            if(i == recordOffset) {
                target.putDouble(i * ValueArrays.REAL64, doubleValue);
            } else if(i < previousLength) {
                target.putDouble(i * ValueArrays.REAL64, IntValueArray.toDouble(source.getInt(i * IntValueArray.BYTES)));
            } else {
                target.putDouble(i * ValueArrays.REAL64, Double.NaN);
            }
        }

        blockEntity.setUnindexedProperty("storage", REAL64_STORAGE);
        blockEntity.setProperty("doubleValues", valueArray);

        return blockEntity;
    }

    private int getStorageMode(Entity blockEntity) {
        Number mode = (Number) blockEntity.getProperty("storage");
        if(mode == null) {
            return NO_STORAGE;
        } else {
            return mode.intValue();
        }
    }

    @Override
    public ColumnView buildView(FormColumnStorage header, QueryResultIterator<Entity> blockIterator) {

        List<Entity> blocks = new ArrayList<>();

        int storage = NO_STORAGE;

        while(blockIterator.hasNext()) {
            Entity block = blockIterator.next();
            storage = Math.max(storage, getStorageMode(block));

            blocks.add(block);
        }

        switch (storage) {
            case NO_STORAGE:
                return new EmptyColumnView(ColumnType.STRING, header.getRecordCount());

            case INT32_STORAGE:
                return buildIntView(header, blocks);
        }

        throw new UnsupportedOperationException("storage: " + storage);
    }

    private ColumnView buildIntView(FormColumnStorage header, List<Entity> blocks) {
        int[] values = new int[header.getRecordCount()];
        Arrays.fill(values, IntValueArray.MISSING);

        for (Entity block : blocks) {
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            Blob blob = (Blob) block.getProperty("intValues");
            if(blob != null) {
                int length = IntValueArray.length(blob);
                IntBuffer buffer = IntValueArray.asBuffer(blob);

                for (int i = 0; i < length; i++) {
                    values[blockStart + i] = buffer.get(i);
                }
            }
        }

        return intColumnFactory.create(values);
    }
}
