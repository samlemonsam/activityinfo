package org.activityinfo.model.type.time;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.RecordFieldTypeClass;
import org.activityinfo.model.type.SingletonTypeClass;

/**
 * Value type that represents a calendar year in the ISO-8601 calendar.
 * There is no representation of time-of-day or time-zone.
 */
public class YearType implements FieldType, TemporalType {

    public interface TypeClass extends SingletonTypeClass, RecordFieldTypeClass {
    }

    public static final TypeClass TYPE_CLASS = new TypeClass() {
        @Override
        public YearValue deserialize(Record record) {
            return new YearValue(record.getInt("year"));
        }

        @Override
        public String getId() {
            return "year";
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }
    };

    public static final YearType INSTANCE = new YearType();

    private YearType() {}

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

}