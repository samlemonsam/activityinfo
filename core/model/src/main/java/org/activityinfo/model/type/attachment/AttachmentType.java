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

import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.type.*;

import static org.activityinfo.json.Json.createObject;

/**
 * @author yuriyz on 8/6/14.
 */
public class AttachmentType implements ParametrizedFieldType {

    public static class TypeClass implements ParametrizedFieldTypeClass, RecordFieldTypeClass {

        private TypeClass() {
        }

        @Override
        public String getId() {
            return "attachment";
        }

        @Override
        public FieldType createType() {
            return new AttachmentType(Cardinality.SINGLE, Kind.ATTACHMENT);
        }


        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            // Explicit type parameter required by GWT's compiler
            Cardinality cardinality = Cardinality.valueOf(parametersObject.<JsonValue>get("cardinality"));
            Kind kind = Kind.valueOf(JsonParsing.fromEnumValue(parametersObject.get("kind")));
            return new AttachmentType(cardinality, kind);
        }

    }

    public static final TypeClass TYPE_CLASS = new TypeClass();

    public enum Kind {
        ATTACHMENT, IMAGE
    }

    private Cardinality cardinality;
    private Kind kind;

    public AttachmentType(Cardinality cardinality, Kind kind) {
        this.cardinality = cardinality;
        this.kind = kind;
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public AttachmentValue parseJsonValue(JsonValue value) {
        if(value.isJsonObject()) {
            value = value.getAsJsonObject().get("values");
        }
        AttachmentValue fieldValue = new AttachmentValue();
        JsonArray array = value.getAsJsonArray();
        for (JsonValue attachmentItem : array.values()) {
            JsonObject attachmentObject = (JsonObject) attachmentItem;
            String mimeType = attachmentObject.get("mimeType").asString();
            String filename = attachmentObject.get("filename").asString();
            String blobId = attachmentObject.get("blobId").asString();

            Attachment attachment = new Attachment(mimeType, filename, blobId);
            attachment.setWidth(attachmentObject.get("width").asInt());
            attachment.setHeight(attachmentObject.get("height").asInt());
            
            fieldValue.getValues().add(attachment);
        }
        return fieldValue;
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitAttachment(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public org.activityinfo.json.JsonObject getParametersAsJson() {
        JsonObject object = createObject();
        object.put("cardinality", cardinality.name().toLowerCase());
        object.put("kind", kind.name().toLowerCase());
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "AttachmentType";
    }
}