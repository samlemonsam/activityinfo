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
