package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.query.shared.columns.DoubleReader;

import javax.annotation.Nullable;
import java.util.Iterator;

public class GeoPointBlock implements BlockManager {

    private final String coordProperty;
    private final NumberBlock latitudeBlock;
    private final NumberBlock longitudeBlock;

    public GeoPointBlock(String fieldName) {
        this.coordProperty = fieldName;
        this.latitudeBlock = new NumberBlock(fieldName + ":lat",
                (DoubleReader)value -> ((GeoPoint)value).getLatitude());
        this.longitudeBlock = new NumberBlock(fieldName + ":lng",
                (DoubleReader)value -> ((GeoPoint)value).getLongitude());
    }

    @Override
    public int getBlockSize() {
        return latitudeBlock.getBlockSize();
    }

    @Override
    public int getMaxFieldSize() {
        return 2;
    }

    @Override
    public String getBlockType() {
        return "geopoint";
    }


    @Override
    public Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue) {
        boolean changed = false;
        Entity updatedBlock;

        updatedBlock = latitudeBlock.update(blockEntity, recordOffset, fieldValue);
        if(updatedBlock != null) {
            changed = true;
            blockEntity = updatedBlock;
        }
        updatedBlock = longitudeBlock.update(blockEntity, recordOffset, fieldValue);
        if(updatedBlock != null) {
            changed = true;
            blockEntity = updatedBlock;
        }
        if(changed) {
            return blockEntity;
        } else {
            return null;
        }
    }

    @Override
    public ColumnView buildView(FormEntity header, TombstoneIndex deleted, Iterator<Entity> blockIterator, String component) {
        if(component.equals("latitude")) {
            return latitudeBlock.buildView(header, deleted, blockIterator);
        } else {
            return longitudeBlock.buildView(header, deleted, blockIterator);
        }
    }

}
