package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.query.shared.columns.DoubleReader;

import javax.annotation.Nullable;
import java.util.Iterator;

public class GeoAreaBlock implements BlockManager {

    private final NumberBlock[] envelopeBlocks;

    public GeoAreaBlock(String fieldName) {
        envelopeBlocks = new NumberBlock[4];
        envelopeBlocks[0] = new NumberBlock(fieldName + ":x1",
                (DoubleReader) value -> ((GeoArea)value).getEnvelope().getX1());
        envelopeBlocks[1] = new NumberBlock(fieldName + ":y1",
                (DoubleReader) value -> ((GeoArea)value).getEnvelope().getX1());
        envelopeBlocks[2] = new NumberBlock(fieldName + ":x2",
                (DoubleReader) value -> ((GeoArea)value).getEnvelope().getX2());
        envelopeBlocks[3] = new NumberBlock(fieldName + ":y2",
                (DoubleReader) value -> ((GeoArea)value).getEnvelope().getY2());

    }

    @Override
    public int getBlockSize() {
        return envelopeBlocks[0].getBlockSize();
    }

    @Override
    public int getMaxFieldSize() {
        return 1;
    }

    @Override
    public String getBlockType() {
        return "geoarea";
    }


    @Override
    public Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue) {
        boolean changed = false;

        for (int i = 0; i < 4; i++) {
            Entity updatedBlock = envelopeBlocks[i].update(blockEntity, recordOffset, fieldValue);
            if(updatedBlock != null) {
                changed = true;
                blockEntity = updatedBlock;
            }
        }
        if(changed) {
            return blockEntity;
        } else {
            return null;
        }
    }

    @Override
    public ColumnView buildView(FormEntity header, TombstoneIndex tombstones, Iterator<Entity> blockIterator, String component) {
        throw new UnsupportedOperationException("TODO");
    }
}

