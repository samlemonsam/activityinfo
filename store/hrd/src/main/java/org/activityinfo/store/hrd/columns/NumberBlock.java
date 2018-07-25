package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.query.EmptyColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.query.shared.columns.DoubleReader;
import org.activityinfo.store.query.shared.columns.IntReader;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.*;

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

    private String formatProperty;
    private String valuesProperty;

    public NumberBlock(String fieldName, DoubleReader doubleReader) {
        this.doubleReader = doubleReader;
        this.intReader = null;
        this.intColumnFactory = DEFAULT_COLUMN_BUILDER;
        this.formatProperty = fieldName + ":format";
        this.valuesProperty = fieldName;
    }

    public NumberBlock(String fieldName, IntReader intReader) {
        this.intReader = intReader;
        this.intColumnFactory = DEFAULT_COLUMN_BUILDER;
        this.doubleReader = null;
        this.formatProperty = fieldName + ":format";
        this.valuesProperty = fieldName;
    }

    public NumberBlock(String fieldName, IntReader intReader, IntColumnFactory intColumnFactory) {
        this.intReader = intReader;
        this.intColumnFactory = intColumnFactory;
        this.doubleReader = null;
        this.formatProperty = fieldName + ":format";
        this.valuesProperty = fieldName;
    }

    @Override
    public int getBlockSize() {
        // Max size per field = 81 920
        return 1024 * 10;
    }

    @Override
    public int getMaxFieldSize() {
        return 4;
    }

    @Override
    public String getBlockType() {
        return "number";
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
        Blob valueArray = (Blob) blockEntity.getProperty(valuesProperty);
        int currentLength = ValueArrays.length(valueArray, IntValueArray.BYTES);
        if(recordOffset < currentLength) {
            valueArray = IntValueArray.update(valueArray, recordOffset, IntValueArray.MISSING);
            blockEntity.setProperty(valuesProperty, valueArray);
            return blockEntity;

        } else {
            /* No change */
            return null;
        }
    }

    private Entity updateInt(Entity blockEntity, int recordOffset, int read) {
        Blob valueArray = (Blob) blockEntity.getProperty(valuesProperty);
        valueArray = IntValueArray.update(valueArray, recordOffset, read);

        blockEntity.setUnindexedProperty(formatProperty, INT32_STORAGE);
        blockEntity.setProperty(valuesProperty, valueArray);
        return blockEntity;
    }

    private Entity updateDoubleMissing(Entity blockEntity, int recordOffset) {
        Blob valueArray = (Blob) blockEntity.getProperty(valuesProperty);
        int currentLength = DoubleValueArray.length(valueArray);
        if(recordOffset < currentLength) {
            valueArray = DoubleValueArray.update(valueArray, recordOffset, Double.NaN);
            blockEntity.setProperty(valuesProperty, valueArray);
            return blockEntity;

        } else {
            /* No change */
            return null;
        }
    }

    private Entity updateDouble(Entity blockEntity, int recordOffset, double doubleValue) {
        Blob valueArray = (Blob) blockEntity.getProperty(valuesProperty);
        valueArray = DoubleValueArray.update(valueArray, recordOffset, doubleValue);

        blockEntity.setUnindexedProperty(formatProperty, REAL64_STORAGE);
        blockEntity.setProperty(valuesProperty, valueArray);

        return blockEntity;
    }


    private Entity migrateToDoubleAndUpdate(Entity blockEntity, int recordOffset, double doubleValue) {
        Blob valueArray = (Blob) blockEntity.getProperty(valuesProperty);

        int previousLength = IntValueArray.length(valueArray);
        int length = Math.max(previousLength, recordOffset + 1);

        byte[] updated = ValueArrays.allocate(length, DoubleValueArray.BYTES);

        ByteBuffer source = ValueArrays.asBuffer(valueArray);
        ByteBuffer target = ValueArrays.asBuffer(updated);

        for (int i = 0; i < length; i++) {
            if(i == recordOffset) {
                target.putDouble(i * DoubleValueArray.BYTES, doubleValue);
            } else if(i < previousLength) {
                target.putDouble(i * DoubleValueArray.BYTES, IntValueArray.toDouble(source.getInt(i * IntValueArray.BYTES)));
            } else {
                target.putDouble(i * DoubleValueArray.BYTES, Double.NaN);
            }
        }

        blockEntity.setUnindexedProperty(formatProperty, REAL64_STORAGE);
        blockEntity.setProperty(valuesProperty, valueArray);

        return blockEntity;
    }

    private int getStorageMode(Entity blockEntity) {
        Number mode = (Number) blockEntity.getProperty(formatProperty);
        if(mode == null) {
            return NO_STORAGE;
        } else {
            return mode.intValue();
        }
    }

    @Override
    public ColumnView buildView(FormEntity header, TombstoneIndex tombstones, Iterator<Entity> blockIterator, String component) {

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
                return buildIntView(header, tombstones, blocks);

            case REAL64_STORAGE:
                return buildDoubleView(header, tombstones, blocks);

        }

        throw new UnsupportedOperationException("storage: " + storage);
    }


    private ColumnView buildIntView(FormEntity header, TombstoneIndex tombstones, List<Entity> blocks) {
        int[] values = new int[header.getRecordCount()];
        Arrays.fill(values, IntValueArray.MISSING);

        for (Entity block : blocks) {
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            int targetIndex = blockStart - tombstones.countDeletedBefore(blockStart);
            BitSet deleted = tombstones.getDeletedBitSet(blockStart, getBlockSize());

            Blob blob = (Blob) block.getProperty(valuesProperty);
            if(blob != null) {
                int length = IntValueArray.length(blob);
                IntBuffer buffer = IntValueArray.asBuffer(blob);

                for (int i = 0; i < length; i++) {
                    if(!deleted.get(i)) {
                        values[targetIndex++] = buffer.get(i);
                    }
                }
            }
        }

        return intColumnFactory.create(values);
    }

    private ColumnView buildDoubleView(FormEntity header, TombstoneIndex tombstones, List<Entity> blocks) {
        double[] values = new double[header.getRecordCount()];
        Arrays.fill(values, Double.NaN);

        for (Entity block : blocks) {
            int blockIndex = (int)(block.getKey().getId() - 1);
            int blockStart = blockIndex * getBlockSize();

            int targetIndex = blockStart - tombstones.countDeletedBefore(blockStart);
            BitSet deleted = tombstones.getDeletedBitSet(blockStart, getBlockSize());

            int storage = getStorageMode(block);
            switch (storage) {
                case NO_STORAGE:
                    break;
                case INT32_STORAGE:
                    readIntBlock(values, block, targetIndex, deleted);
                    break;
                case REAL64_STORAGE:
                    readDoubleBlock(values, block, targetIndex, deleted);
                    break;
                default:
                    throw new UnsupportedOperationException("storage: " + storage);
            }
        }

        return new DoubleArrayColumnView(values);

    }

    private void readDoubleBlock(double[] values, Entity block, int targetIndex, BitSet deleted) {
        Blob blob = (Blob) block.getProperty(valuesProperty);
        if(blob != null) {
            int length = DoubleValueArray.length(blob);
            DoubleBuffer buffer = DoubleValueArray.asBuffer(blob);

            for (int i = 0; i < length; i++) {
                if(!deleted.get(i)) {
                    values[targetIndex++] = buffer.get(i);
                }
            }
        }
    }

    private void readIntBlock(double[] values, Entity block, int targetIndex, BitSet deleted) {
        Blob blob = (Blob) block.getProperty(valuesProperty);
        if(blob != null) {
            int length = IntValueArray.length(blob);
            IntBuffer buffer = IntValueArray.asBuffer(blob);

            for (int i = 0; i < length; i++) {
                if(!deleted.get(i)) {
                    int intValue = buffer.get(i);
                    if(intValue != IntValueArray.MISSING) {
                        values[targetIndex] = intValue;
                    }
                    targetIndex++;
                }
            }
        }
    }

}
