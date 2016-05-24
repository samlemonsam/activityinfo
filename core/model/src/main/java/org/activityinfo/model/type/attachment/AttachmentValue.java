package org.activityinfo.model.type.attachment;
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

import com.google.common.collect.Lists;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

import java.util.List;

/**
 * @author yuriyz on 8/6/14.
 */
public class AttachmentValue implements FieldValue, IsRecord {

    private final List<Attachment> values = Lists.newArrayList();

    @Override
    public FieldTypeClass getTypeClass() {
        return AttachmentType.TYPE_CLASS;
    }

    public AttachmentValue() {
    }

    public AttachmentValue(Attachment imageRowValue) {
        values.add(imageRowValue);
    }

    public List<Attachment> getValues() {
        return values;
    }

    public boolean hasValues() {
        return !values.isEmpty();
    }

    public List<Record> getValuesAsRecords() {
        final List<Record> result = Lists.newArrayList();
        for (Attachment value : values) {
            result.add(value.asRecord());
        }
        return result;
    }

    @Override
    public Record asRecord() {
        return new Record()
                .set(TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
                .set("values", getValuesAsRecords());
    }

    public static AttachmentValue fromRecord(Record record) {
        AttachmentValue value = new AttachmentValue();
        List<Record> recordList = record.getRecordList("values");
        for (Record r : recordList) {
            value.getValues().add(Attachment.fromRecord(r));
        }
        return value;
    }

    public static AttachmentValue fromJson(String json) {
        return fromRecord(Resources.recordFromJson(json));
    }

    public static AttachmentValue fromJsonSilently(String json) {
        try {
            return fromJson(json);
        } catch (Exception e) {
            return null;
        }
    }
}
