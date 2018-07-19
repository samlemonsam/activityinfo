package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Entity;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.store.hrd.entity.FormEntity;

import javax.annotation.Nullable;
import java.util.Iterator;

public class GeoPointBlock implements BlockManager {

    private static final String COORD_PROPERTY = "coords";

    @Override
    public int getBlockSize() {
        // 5_120 * 16 = 80k per block
        return 1024 * 5;
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

        if(DoubleValueArray.update(blockEntity, COORD_PROPERTY, recordOffset * 2, lat, lng)) {
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
