package org.activityinfo.model.type.barcode;

import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.*;

/**
 * A value types that describes a real-valued barcode and its units.
 */
public class BarcodeType implements FieldType {


    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {

        public static final String TYPE_ID = "BARCODE";

        @Override
        public String getId() {
            return TYPE_ID;
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

        @Override
        public FieldValue deserialize(Record record) {
            return BarcodeValue.fromRecord(record);
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
    public String toString() {
        return "BarcodeType";
    }
}
