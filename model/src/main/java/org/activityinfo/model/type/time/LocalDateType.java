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

/**
 * Value type that represents a date in the ISO-8601 calendar.
 * There is no representation of time-of-day or time-zone.
 *
 * <blockquote>
 * The “local” terminology is familiar from Joda-Time and comes originally from the ISO-8601 date and time standard.
 * It relates specifically to the absence of a time-zone. In effect, a local date is a description of a date,
 * such as the “5th April 2014”. That particular local date will start at different points on the time-line
 * depending on where on the Earth you are. Thus the local date will start in Australia 10 hours before it
 * starts in London and 18 hours before it starts in San Francisco. -- Stephen Colebourn in
 * <a href="http://www.infoq.com/articles/java.time">InfoQ</a>
 * </blockquote>
 *
 */
public class LocalDateType implements PeriodType {

    public static final String TYPE_ID = "date";

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

    public static final LocalDateType INSTANCE = new LocalDateType();


    private LocalDateType() { }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public LocalDate parseJsonValue(JsonValue value) {
        if(value.isJsonObject()) {
            value = value.get("value");
        }
        return LocalDate.parse(value.asString());
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {

        return visitor.visitLocalDate(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public String toString() {
        return "LocalDateType";
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public PeriodValue fromSubFormKey(RecordRef ref) {
        String subRecordId = ref.getRecordId().asString();
        String dateKey = subRecordId.substring(subRecordId.length() - 10);
        return LocalDate.parse(dateKey);
    }

    @Override
    public PeriodValue containingDate(LocalDate localDate) {
        return localDate;
    }
}
