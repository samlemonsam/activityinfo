package org.activityinfo.model.type;

import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;

public class FieldValues {

    /**
     * Deserializes the {@code FieldValue} named {@code fieldName} from {@code resource} if the
     * field value matches the given {@code typeClass}, otherwise {@code null}
     *
     */
    public static <T extends FieldValue> T readFieldValueIfType(Resource resource,
                                                                String fieldName,
                                                                RecordFieldTypeClass typeClass) {

        Record record = resource.isRecord(fieldName);
        if(record != null) {
            if (record.getString(FieldValue.TYPE_CLASS_FIELD_NAME).equals(typeClass.getId())) {
                return (T)typeClass.deserialize(record);
            }
        }
        return null;
    }

    public static FieldValue readFieldValueIfType(Resource instance, String fieldName, FieldTypeClass typeClass) {
        if(typeClass == TextType.TYPE_CLASS) {
            return TextValue.valueOf(instance.isString(fieldName));

        } else if(typeClass == BooleanType.TYPE_CLASS) {
            return BooleanFieldValue.valueOf(instance.isBoolean(fieldName));

        } else if(typeClass instanceof RecordFieldTypeClass) {
            return readFieldValueIfType(instance, fieldName, (RecordFieldTypeClass)typeClass);

        } else {
            throw new UnsupportedOperationException(typeClass.getId());
        }
    }
}
