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

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

public class Instant implements IsRecord, FieldValue {

    private long time;

    public Instant(double time) {
        this.time = (long) time;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return InstantType.TYPE_CLASS;
    }

    @Override
    public Record asRecord() {
        return new Record()
                .set(TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
                .set("time", time);
    }

    public static Instant fromRecord(Record record) {
        return new Instant(record.getDouble("time"));
    }
}