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
    public EpiWeek parseJsonValue(JsonValue value) {
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
    public EpiWeek fromSubFormKey(RecordRef ref) {
        String subRecordId = ref.getRecordId().asString();
        int delimiter = subRecordId.lastIndexOf('-');
        return EpiWeek.parse(subRecordId.substring(delimiter + 1));
    }

    @Override
    public PeriodValue containingDate(LocalDate localDate) {
        return EpiWeek.weekOf(localDate);
    }
}
