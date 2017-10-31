package org.activityinfo.model.type.time;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.*;

public class EpiWeekType implements PeriodType {

    public static final String TYPE_ID = "epiweek";

    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {
        @Override
        public String getId() {
            return TYPE_ID;
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

    };

    public static final EpiWeekType INSTANCE = new EpiWeekType();

    private EpiWeekType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        return EpiWeek.parse(value.asString());
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitWeek(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public PeriodValue fromSubFormKey(RecordRef ref) {
        String subRecordId = ref.getRecordId().asString();
        String weekKey = subRecordId.substring(subRecordId.length() - 7);
        return EpiWeek.parse(weekKey);
    }

    @Override
    public PeriodValue containingDate(LocalDate localDate) {
        return EpiWeek.weekOf(localDate);
    }
}
