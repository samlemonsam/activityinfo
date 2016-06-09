package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Text;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates Field Observers for specific field types
 */
public class FieldConverters {

    public static FieldConverter<?> forType(final FieldType type) {
        if (type instanceof QuantityType) {
            return quantity((QuantityType) type);
        } else if (type instanceof TextType) {
            return TEXT;
        } else if (type instanceof BarcodeType) {
            return BARCODE;
        } else if(type instanceof ReferenceType) {
            return REFERENCE;
        } else if(type instanceof NarrativeType) {
            return NARRATIVE;
        } else if(type instanceof GeoPointType) {
            return GEO_POINT;
        } else if(type instanceof LocalDate) {
            return LOCAL_DATE;
        } else if(type.getTypeClass() instanceof RecordFieldTypeClass) {
            return recordType((RecordFieldTypeClass) type.getTypeClass());
        } else {
            throw new UnsupportedOperationException("Type: " + type);
        }
    }
    
    public static FieldConverter<?> forParentField() {
        return new FieldConverter<ReferenceValue>() {
            @Override
            public Object toHrdProperty(ReferenceValue value) {
                return value.getResourceId().asString();
            }

            @Override
            public ReferenceValue toFieldValue(Object hrdValue) {
                return new ReferenceValue(ResourceId.valueOf((String)hrdValue));
            }
        };
    }
    
    public static FieldConverter<Quantity> quantity(final QuantityType type) {
        return new FieldConverter<Quantity>() {

            @Override
            public Double toHrdProperty(Quantity value) {
                return value.getValue();
            }

            @Override
            public Quantity toFieldValue(Object hrdValue) {
                Number number = (Number) hrdValue;
                return new Quantity(number.doubleValue(), type.getUnits());
            }
        };
    }
    
    public static final FieldConverter<TextValue> TEXT = new FieldConverter<TextValue>() {

        @Override
        public Object toHrdProperty(TextValue value) {
            return value.asString();
        }

        @Override
        public TextValue toFieldValue(Object hrdValue) {
            return TextValue.valueOf((String) hrdValue);
        }
    };
    
    public static final FieldConverter<BarcodeValue> BARCODE = new FieldConverter<BarcodeValue>() {

        @Override
        public Object toHrdProperty(BarcodeValue value) {
            return value.asString();
        }

        @Override
        public BarcodeValue toFieldValue(Object hrdValue) {
            return BarcodeValue.valueOf((String)hrdValue);
        }
    };
    
    public static final FieldConverter<ReferenceValue> REFERENCE = new FieldConverter<ReferenceValue>() {
    
        @Override
        public Object toHrdProperty(ReferenceValue value) {
            if (value.getResourceIds().isEmpty()) {
                return null;
            } else if (value.getResourceIds().size() == 1) {
                return value.getResourceId().asString();
            } else {
                List<String> ids = new ArrayList<>();
                for (ResourceId resourceId : value.getResourceIds()) {
                    ids.add(resourceId.asString());
                }
                return ids;
            }
        }

        @Override
        public ReferenceValue toFieldValue(Object hrdValue) {
            Set<ResourceId> resourceIdSet = new HashSet<>();
            if (hrdValue instanceof String) {
                resourceIdSet.add(ResourceId.valueOf((String) hrdValue));
            } else if (hrdValue instanceof List) {
                List<?> list = (List<?>) hrdValue;
                for (Object id : list) {
                    if (id instanceof String) {
                        resourceIdSet.add(ResourceId.valueOf((String) id));
                    }
                }
            }
            return new ReferenceValue(resourceIdSet);
        }
    };
    
    public static final FieldConverter<NarrativeValue> NARRATIVE = new FieldConverter<NarrativeValue>() {
        @Override
        public Object toHrdProperty(NarrativeValue value) {
            return new Text(value.getText());
        }

        @Override
        public NarrativeValue toFieldValue(Object hrdValue) {
            if(hrdValue instanceof Text) {
                return NarrativeValue.valueOf(((Text) hrdValue).getValue());
            } else {
                return null;
            }
        }
    };
    
    public static final FieldConverter<GeoPoint> GEO_POINT = new FieldConverter<GeoPoint>() {
        @Override
        public Object toHrdProperty(GeoPoint value) {
            return new GeoPt((float)value.getLatitude(), (float)value.getLongitude());
        }

        @Override
        public GeoPoint toFieldValue(Object hrdValue) {
            GeoPoint point = (GeoPoint) hrdValue;
            return new GeoPoint(point.getLatitude(), point.getLongitude());
        }
    };
    
    public static final FieldConverter<LocalDate> LOCAL_DATE = new FieldConverter<LocalDate>() {
     
        @Override
        public Object toHrdProperty(LocalDate value) {
            // Use simple YYYY-MM-DD
            return value.toString();
        }

        @Override
        public LocalDate toFieldValue(Object hrdValue) {
            String stringValue = (String) hrdValue;
            return LocalDate.parse(stringValue);
        }
    };
    
    public static final FieldConverter<FieldValue> recordType(final RecordFieldTypeClass typeClass) {
        return new FieldConverter<FieldValue>() {
            @Override
            public Object toHrdProperty(FieldValue value) {
                Record recordValue = ((IsRecord) value).asRecord();
                return RecordSerialization.toEmbeddedEntity(recordValue);
            }

            @Override
            public FieldValue toFieldValue(Object hrdValue) {
                EmbeddedEntity embeddedEntity = (EmbeddedEntity) hrdValue;
                Record record = RecordSerialization.fromEmbeddedEntity(embeddedEntity);
                return typeClass.deserialize(record);
            }
        };
    }
}
