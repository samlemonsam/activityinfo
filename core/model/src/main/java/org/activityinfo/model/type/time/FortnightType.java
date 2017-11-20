package org.activityinfo.model.type.time;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.*;

public class FortnightType implements PeriodType {


    public static final String TYPE_ID = "fortnight";

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

    public static final FortnightType INSTANCE = new FortnightType();

    private FortnightType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FortnightValue parseJsonValue(JsonValue value) {
        EpiWeek startWeek = EpiWeekType.INSTANCE.parseJsonValue(value);
        return new FortnightValue(startWeek);
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitFortnight(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public FortnightValue fromSubFormKey(RecordRef ref) {
        // s0417565614-2017W2-3

        String subRecordId = ref.getRecordId().asString();
        int endWeekDelimiter = subRecordId.lastIndexOf('-');
        int delimiter = subRecordId.lastIndexOf('-', endWeekDelimiter-1);

        EpiWeek firstWeek = EpiWeek.parse(subRecordId.substring(delimiter+1, endWeekDelimiter));
        return new FortnightValue(firstWeek);
    }

    @Override
    public PeriodValue containingDate(LocalDate localDate) {
        EpiWeek week = EpiWeek.weekOf(localDate);
        if(week.getWeekInYear() % 2 == 0) {
            return new FortnightValue(week.previous());
        } else {
            return new FortnightValue(week);
        }
    }


}
