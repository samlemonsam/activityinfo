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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuriyz on 8/6/14.
 */
public class AttachmentValue implements FieldValue {

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

    public AttachmentValue(List<Attachment> list) {
        values.addAll(list);
    }

    public List<Attachment> getValues() {
        return values;
    }

    public boolean hasValues() {
        return !values.isEmpty();
    }

    @Override
    public JsonElement toJsonElement() {
        JsonArray array = new JsonArray();
        for (Attachment value : values) {
            array.add(value.toJsonElement());
        }
        return array;
    }

    public static AttachmentValue fromJson(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject object = jsonParser.parse(json).getAsJsonObject();
        JsonArray array = object.getAsJsonArray("values");

        List<Attachment> list = new ArrayList<>();
        for (JsonElement value : array) {
            list.add(Attachment.fromJson(value.getAsJsonObject()));
        }

        return new AttachmentValue(list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachmentValue that = (AttachmentValue) o;

        return !(values != null ? !values.equals(that.values) : that.values != null);

    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }

    public static AttachmentValue fromJsonSilently(String json) {
        try {
            return fromJson(json);
        } catch (Exception e) {
            return null;
        }
    }


}
