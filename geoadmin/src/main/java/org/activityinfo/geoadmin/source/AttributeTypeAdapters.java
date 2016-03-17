package org.activityinfo.geoadmin.source;


import com.vividsolutions.jts.geom.Polygonal;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.opengis.feature.type.PropertyDescriptor;

public enum AttributeTypeAdapters implements AttributeTypeAdapter {

    AREA {
        @Override
        public FieldType createType() {
            return GeoAreaType.INSTANCE;
        }
    },

    QUANTITY {
        @Override
        public FieldType createType() {
            return QuantityType.TYPE_CLASS.createType();
        }
    },
    
    TEXT {
        @Override
        public FieldType createType() {
            return TextType.INSTANCE;
        }
    };


    public abstract FieldType createType();


    public static AttributeTypeAdapter of(PropertyDescriptor descriptor) {
        Class<?> type = descriptor.getType().getBinding();
        if (type.equals(String.class)) {
            return TEXT;
        } else if (Polygonal.class.isAssignableFrom(type)) {
            return AREA;
        } else if (Number.class.isAssignableFrom(type)) {
            return QUANTITY;
        } else {
            throw new IllegalArgumentException(type.getName());
        }
    }

}
