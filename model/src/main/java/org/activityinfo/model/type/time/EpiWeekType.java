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
    public PeriodValue parseString(String string) {
        return EpiWeek.parse(string);
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
