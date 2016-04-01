package org.activityinfo.model.type;
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
import com.google.common.collect.Maps;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * It's not really type but convenient way to keep metadata of formclass as formfield (typically invisible).
 * Use cases:
 *  - label representation on FormDesigner
 *
 * @author yuriyz on 03/28/2016.
 */
public class MetadataType implements ParametrizedFieldType {

    public enum TextStyle {
        PLAIN("plain", I18N.CONSTANTS.plain(), "", ""),
        BOLD("bold", I18N.CONSTANTS.bold(), "<b>", "</b>");

        private String value;
        private String label;
        private String htmlStartTag;
        private String htmlEndTag;

        TextStyle(String value, String label, String htmlStartTag, String htmlEndTag) {
            this.value = value;
            this.label = label;
            this.htmlStartTag = htmlStartTag;
            this.htmlEndTag = htmlEndTag;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public ResourceId getResourceId() {
            return ResourceId.valueOf(value);
        }

        public static TextStyle fromValue(String textStyle) {
            for (TextStyle style : values()) {
                if (style.getValue().equalsIgnoreCase(textStyle)) {
                    return style;
                }
            }

            return null;
        }

        public String applyStyle(String html) {
            return getHtmlStartTag() + html + getHtmlEndTag();
        }

        public String getHtmlStartTag() {
            return htmlStartTag;
        }

        public String getHtmlEndTag() {
            return htmlEndTag;
        }
    }

    public static class LabelTypeClass implements ParametrizedFieldTypeClass, Serializable {

        public LabelTypeClass() {
        }

        @Override
        public String getId() {
            return "METADATA_TYPE";
        }

        @Override
        public MetadataType createType() {
            return new MetadataType();
        }

        @Override
        public MetadataType deserializeType(Record typeParameters) {
            MetadataType type = new MetadataType();
            type.getValues().putAll(typeParameters.getRecord("values").getProperties());
            return type;
        }

        @Override
        public FormClass getParameterFormClass() {
            FormClass formClass = new FormClass(ResourceIdPrefixType.TYPE.id("metadata"));

            List<EnumItem> items = Lists.newArrayList();
            for (TextStyle style : TextStyle.values()) {
                EnumItem item = new EnumItem(style.getResourceId(), style.getLabel());
                items.add(item);
            }

            EnumType enumType = new EnumType(Cardinality.SINGLE, items);
            enumType.getDefaultValues().add(new EnumItem(TextStyle.PLAIN.getResourceId(), TextStyle.PLAIN.getLabel()));

            formClass.addElement(new FormField(ResourceId.valueOf("text_style"))
                    .setType(enumType)
                    .setLabel(I18N.CONSTANTS.style()));

            return formClass;
        }
    }

    public static final LabelTypeClass LABEL_TYPE_CLASS = new LabelTypeClass();

    private HashMap<String, Object> values = Maps.newHashMap();

    public MetadataType() {
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return LABEL_TYPE_CLASS;
    }

    @Override
    public Record getParameters() {
        return new Record()
                .set("values", new Record().setAll(values))
                .set("classId", getTypeClass().getParameterFormClass().getId());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "MetadataType{" +
                "values='" + values + '\'' +
                '}';
    }

    public String applyStyle(String html) {
        Object textStyleStr = values.get("text_style");
        if (textStyleStr instanceof String) {
            TextStyle textStyle = TextStyle.fromValue((String) textStyleStr);
            if (textStyle != null) {
                return textStyle.applyStyle(html);
            }
        }
        return html;
    }
}
