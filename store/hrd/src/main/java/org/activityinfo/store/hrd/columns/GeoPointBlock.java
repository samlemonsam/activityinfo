package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.store.hrd.entity.ColumnDescriptor;
import org.activityinfo.store.hrd.entity.FormEntity;

import javax.annotation.Nullable;
import java.util.Iterator;

public class GeoPointBlock implements BlockManager {

    private final String coordProperty;

    public GeoPointBlock(String fieldName) {
        this.coordProperty = fieldName;
    }

    @Override
    public int getBlockRowSize() {
        // 5_120 * 16 = 80k per block
        return 1024 * 5;
    }

    @Override
    public int getMaxFieldSize() {
        return 4; // Max entity size = 327_680 bytes
    }

    @Override
    public String getBlockType() {
        return "geopoint";
    }


    @Override
    public Entity update(Entity blockEntity, int recordOffset, @Nullable FieldValue fieldValue) {
        double lat;
        double lng;
        if(fieldValue instanceof GeoPoint) {
            GeoPoint pointValue = (GeoPoint) fieldValue;
            lat = pointValue.getLatitude();
            lng = pointValue.getLongitude();
        } else {
            lat = lng = Double.NaN;
        }

        if(DoubleValueArray.update(blockEntity, coordProperty, recordOffset * 2, lat, lng)) {
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
