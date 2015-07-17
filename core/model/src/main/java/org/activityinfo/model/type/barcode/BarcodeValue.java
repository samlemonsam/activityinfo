package org.activityinfo.model.type.barcode;

import com.google.common.base.Strings;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.HasStringValue;

public class BarcodeValue implements FieldValue, IsRecord, HasStringValue {

    private final String code;

    public static BarcodeValue valueOf(String code) {
        if(Strings.isNullOrEmpty(code)) {
            return null;
        } else {
            return new BarcodeValue(code);
        }
    }

    private BarcodeValue(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String asString() {
        return code;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return BarcodeType.TYPE_CLASS;
    }

    @Override
    public Record asRecord() {
        return new Record()
                .set(TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
                .set("code", code);
    }

    public static BarcodeValue fromRecord(Record record) {
        return new BarcodeValue(record.getString("code"));
    }

}
