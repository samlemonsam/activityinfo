/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Text;
import org.activityinfo.json.JsonValue;
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
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.model.type.time.PeriodValue;

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
            return new ReferenceFieldConverter((ReferenceType) type);
        } else if(type instanceof NarrativeType) {
            return NARRATIVE;
        } else if(type instanceof GeoPointType) {
            return GEO_POINT;
        } else if(type instanceof LocalDate) {
            return LOCAL_DATE;
        } else if(type instanceof PeriodType) {
            return new PeriodConverter(((PeriodType) type));
        } else {
            return recordType(type);
        }
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
                return new Quantity(number.doubleValue());
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

    private static class PeriodConverter implements FieldConverter<PeriodValue> {

        private final PeriodType periodType;

        private PeriodConverter(PeriodType periodType) {
            this.periodType = periodType;
        }

        @Override
        public Object toHrdProperty(PeriodValue value) {
            return value.toString();
        }

        @Override
        public PeriodValue toFieldValue(Object hrdValue) {
            if(hrdValue instanceof String) {
                return periodType.parseString((String) hrdValue);
            }
            return null;
        }
    }

    private static class ReferenceFieldConverter implements FieldConverter<ReferenceValue> {

        private ResourceId formId;

        private ReferenceFieldConverter(ReferenceType type) {
            if(type.getRange().size() == 1) {
                this.formId = type.getRange().iterator().next();
            } else {
                this.formId = null;
            }
        }

        @Override
        public Object toHrdProperty(ReferenceValue value) {
            if (value.getReferences().isEmpty()) {
                return null;
            } else if (value.getReferences().size() == 1) {
                return value.getOnlyReference().toQualifiedString();
            } else {
                List<String> ids = new ArrayList<>();
                for (RecordRef ref : value.getReferences()) {
                    ids.add(ref.toQualifiedString());
                }
                return ids;
            }
        }

        @Override
        public ReferenceValue toFieldValue(Object hrdValue) {
            Set<RecordRef> refSet = new HashSet<>();
            if (hrdValue instanceof String) {
                refSet.add(parseRef((String) hrdValue));
            } else if (hrdValue instanceof List) {
                List<?> list = (List<?>) hrdValue;
                for (Object id : list) {
                    if (id instanceof String) {
                        refSet.add(parseRef((String) id));
                    }
                }
            }
            return new ReferenceValue(refSet);
        }

        private RecordRef parseRef(String hrdValue) {
            int separatorPos = hrdValue.indexOf(':');
            if(separatorPos == -1) {
                if(formId == null) {
                    throw new RuntimeException("Encountered legacy unqualified reference in a reference with" +
                            " multiple ranges. Ref: " + hrdValue);
                }
                return new RecordRef(formId, ResourceId.valueOf(hrdValue));
            } else {
                return RecordRef.fromQualifiedString(hrdValue);
            }
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
            GeoPt point = (GeoPt) hrdValue;
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
    
    public static final FieldConverter<FieldValue> recordType(final FieldType type) {
        return new FieldConverter<FieldValue>() {
            @Override
            public Object toHrdProperty(FieldValue value) {
                return FormConverter.toPropertyValue(value.toJson());
            }

            @Override
            public FieldValue toFieldValue(Object hrdValue) {
                JsonValue element = FormConverter.fromPropertyValue(hrdValue);
                return type.parseJsonValue(element);
            }
        };
    }
}
