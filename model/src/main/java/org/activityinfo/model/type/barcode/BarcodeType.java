package org.activityinfo.model.type.barcode;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.*;

/**
 * A value types that describes a real-valued barcode and its units.
 */
public class
BarcodeType implements FieldType {


    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {

        public static final String TYPE_ID = "barcode";

        @Override
        public String getId() {
            return TYPE_ID;
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

    };

    public static final BarcodeType INSTANCE = new BarcodeType();

    private BarcodeType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        return BarcodeValue.valueOf(value.asString());
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitBarcode(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public String toString() {
        return "BarcodeType";
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }

}
