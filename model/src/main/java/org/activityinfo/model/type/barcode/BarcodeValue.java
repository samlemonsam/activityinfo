package org.activityinfo.model.type.barcode;

import com.google.common.base.Strings;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.HasStringValue;

public class BarcodeValue implements FieldValue, HasStringValue {

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
    public JsonValue toJsonElement() {
        return Json.createFromNullable(code);
    }

    public static FieldValue valueOf(JsonValue element) {
        return BarcodeValue.valueOf(JsonParsing.toNullableString(element));
    }
}
