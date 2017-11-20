package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;

import java.util.Random;


public class GeoPointGenerator implements Supplier<FieldValue> {


    private final Random random = new Random(235564434L);
    private double probabilityMissing;


    public GeoPointGenerator(FormField field) {
        if(field.isRequired()) {
            probabilityMissing = 0;
        } else {
            probabilityMissing = 0.20;
        }
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        } else {
            double latitude = random.nextDouble() * 180d - 90d;
            double longitude = random.nextDouble() * 360d - 180d;
            return new GeoPoint(latitude, longitude);
        }
    }
}
