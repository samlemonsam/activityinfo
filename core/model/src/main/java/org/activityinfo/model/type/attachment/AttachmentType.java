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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

/**
 * @author yuriyz on 8/6/14.
 */
public class AttachmentType implements ParametrizedFieldType {

    public static class TypeClass implements ParametrizedFieldTypeClass, RecordFieldTypeClass {

        private TypeClass() {
        }

        @Override
        public String getId() {
            return "ATTACHMENT";
        }

        @Override
        public FieldType createType() {
            return new AttachmentType(Cardinality.SINGLE, Kind.ATTACHMENT);
        }

        @Override
        public FieldValue deserialize(Record record) {
            return AttachmentValue.fromRecord(record);
        }

        @Override
        public AttachmentType deserializeType(Record typeParameters) {
            EnumValue cardinalityValue = (EnumValue) EnumType.TYPE_CLASS.deserialize(typeParameters.getRecord("cardinality"));
            EnumValue kindValue = (EnumValue) EnumType.TYPE_CLASS.deserialize(typeParameters.getRecord("kind"));
            return new AttachmentType(
                    Cardinality.valueOf(cardinalityValue.getValueId().asString()),
                    Kind.valueOf(kindValue.getValueId().asString()));
        }

        @Override
        public FormClass getParameterFormClass() {
            EnumType cardinalityType = (EnumType) EnumType.TYPE_CLASS.createType();
            cardinalityType.getValues().add(new EnumItem(ResourceId.valueOf("single"), I18N.CONSTANTS.single()));
            cardinalityType.getValues().add(new EnumItem(ResourceId.valueOf("multiple"), I18N.CONSTANTS.multiple()));

            EnumType kindType = (EnumType) EnumType.TYPE_CLASS.createType();
            kindType.getValues().add(new EnumItem(ResourceId.valueOf(Kind.ATTACHMENT.name()), I18N.CONSTANTS.attachment()));
            kindType.getValues().add(new EnumItem(ResourceId.valueOf(Kind.IMAGE.name()), I18N.CONSTANTS.image()));

            FormClass formClass = new FormClass(ResourceIdPrefixType.TYPE.id("image"));
            formClass.addElement(new FormField(ResourceId.valueOf("kind"))
                            .setType(kindType)
                            .setLabel(I18N.CONSTANTS.type())
                            .setDescription(I18N.CONSTANTS.attachmentTypeDescription())
                            .setVisible(false)
            );
            return formClass;
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
    public FieldValue parseJsonValue(JsonElement value) {
        AttachmentValue fieldValue = new AttachmentValue();
        JsonArray array = (JsonArray) value;
        for (JsonElement attachmentItem : array) {
            JsonObject attachmentObject = (JsonObject) attachmentItem;
            String mimeType = attachmentObject.get("mimeType").getAsString();
            String filename = attachmentObject.get("filename").getAsString();
            String blobId = attachmentObject.get("blobId").getAsString();

            Attachment attachment = new Attachment(mimeType, filename, blobId);
            attachment.setWidth(attachmentObject.get("width").getAsInt());
            attachment.setHeight(attachmentObject.get("height").getAsInt());
            
            fieldValue.getValues().add(attachment);
        }
        return fieldValue;
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
    public Record getParameters() {
        return new Record()
                .set("classId", getTypeClass().getParameterFormClass().getId())
                .set("cardinality", new EnumValue(ResourceId.valueOf(cardinality.name())).asRecord())
                .set("kind", new EnumValue(ResourceId.valueOf(kind.name())).asRecord());
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