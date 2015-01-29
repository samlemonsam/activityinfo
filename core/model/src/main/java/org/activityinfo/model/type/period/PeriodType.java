package org.activityinfo.model.type.period;
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
import org.activityinfo.model.type.*;

/**
 * @author yuriyz on 01/27/2015.
 */
public class PeriodType implements FieldType {

    public static final PeriodType INSTANCE = new PeriodType();

    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {
        @Override
        public String getId() {
            return "PERIOD";
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

        @Override
        public FieldValue deserialize(Record record) {
            return PeriodValue.fromRecord(record);
        }
    };

    public PeriodType() {
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

}
