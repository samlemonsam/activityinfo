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
package org.activityinfo.model.form;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.activityinfo.json.Json.createObject;

/**
 * The smallest logical unit of data entry.
 */
public class FormField extends FormElement {

    private final ResourceId id;
    private String code;
    private String label;
    private String description;
    private String relevanceConditionExpression;
    private FieldType type;
    private boolean visible = true;
    private Set<ResourceId> superProperties = Sets.newHashSet();
    private boolean required;
    private boolean key;

    public FormField(ResourceId id) {
        checkNotNull(id);
        this.id = id;
    }

    public ResourceId getId() {
        return id;
    }

    public String getName() {
        return id.asString();
    }

    /**
     * @return user-assigned code for this field that can be
     * used in expressions.
     */
    public String getCode() {
        return code;
    }


    public FormField setCode(String code) {
        this.code = code;
        return this;
    }

    public boolean hasCode() {
        return code != null;
    }

    /**
     *
     * @return true if {@code} is a valid code, starting with a letter and
     * containing only letters, numbers, and the underscore symbol
     */
    public static boolean isValidCode(String code) {
        return code != null && code.matches("^[A-Za-z][A-Za-z0-9_]*");
    }

    @Override
    @Nonnull
    public String getLabel() {
        return label;
    }

    public FormField setLabel(String label) {
        assert label != null;
        this.label = label;
        return this;
    }

    public boolean hasRelevanceCondition() {
        return !Strings.isNullOrEmpty(relevanceConditionExpression);
    }

    public String getRelevanceConditionExpression() {
        return relevanceConditionExpression;
    }

    public FormField setRelevanceConditionExpression(String relevanceConditionExpression) {
        this.relevanceConditionExpression = relevanceConditionExpression;
        return this;
    }

    /**
     * @return an extended description of this field, presented to be
     * presented to the user during data entry
     */
    public String getDescription() {
        return description;
    }

    public FormField setDescription(String description) {
        this.description = description;
        return this;
    }

    public FieldType getType() {
        assert type != null : "type is missing for " + id;
        return type;
    }

    public FormField setType(FieldType type) {
        this.type = type;
        return this;
    }


    public boolean isKey() {
        return key;
    }

    public FormField setKey(boolean key) {
        this.key = key;
        return this;
    }

    /**
     *
     * @return true if this field requires a response before submitting the form
     */
    public boolean isRequired() {
        return required;
    }

    public FormField setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public boolean hasRelevanceConditionExpression() {
        return !Strings.isNullOrEmpty(relevanceConditionExpression);
    }

    /**
     * @return true if this field is visible to the user
     */
    public boolean isVisible() {
        return visible;
    }

    public FormField setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormField formField = (FormField) o;

        if (id != null ? !id.equals(formField.id) : formField.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FormField{" +
                "id=" + id +
                ", label=" + label +
                ", type=" + type.getTypeClass().getId() +
                '}';
    }

    public Set<ResourceId> getSuperProperties() {
        return superProperties;
    }

    public void addSuperProperty(ResourceId propertyId) {
        superProperties.add(propertyId);
    }

    public void setSuperProperties(Set<ResourceId> superProperties) {
        this.superProperties = superProperties;
    }

    public FormField setSuperProperty(ResourceId superProperty) {
        this.superProperties = Collections.singleton(superProperty);
        return this;
    }

    public boolean isSubPropertyOf(ResourceId parentProperty) {
        return this.superProperties.contains(parentProperty);
    }

    @Override
    public JsonValue toJsonObject() {
        JsonValue object = createObject();
        object.put("id", id.asString());
        object.put("code", code);
        object.put("label", label);
        object.put("description", description);
        object.put("relevanceCondition", relevanceConditionExpression);
        object.put("visible", visible);
        object.put("required", required);

        object.put("type", type.getTypeClass().getId());

        if(key) {
            object.put("key", true);
        }

        if(!superProperties.isEmpty()) {
            JsonValue superPropertiesArray = Json.createArray();
            for (ResourceId superProperty : superProperties) {
                superPropertiesArray.add(Json.createFromNullable(superProperty.asString()));
            }
            object.put("superProperties", superPropertiesArray);
        }

        if(type instanceof ParametrizedFieldType) {
            object.put("typeParameters", ((ParametrizedFieldType) type).getParametersAsJson());
        }
        
        return object;
    }


    public static FormField fromJson(JsonValue jsonObject) {
        FormField field = new FormField(ResourceId.valueOf(jsonObject.get("id").asString()));
        field.setLabel(Strings.nullToEmpty(JsonParsing.toNullableString(jsonObject.get("label"))));
        field.setCode(JsonParsing.toNullableString(jsonObject.get("code")));
        field.setDescription(JsonParsing.toNullableString(jsonObject.get("description")));

        if(jsonObject.hasKey("relevanceCondition")) {
            field.setRelevanceConditionExpression(JsonParsing.toNullableString(jsonObject.get("relevanceCondition")));
        } else if(jsonObject.hasKey("relevanceConditionExpression")) {
            field.setRelevanceConditionExpression(JsonParsing.toNullableString(jsonObject.get("relevanceConditionExpression")));
        }
        
        if(jsonObject.hasKey("visible")) {
            field.setVisible(jsonObject.get("visible").asBoolean());
        }
        if(jsonObject.hasKey("required")) {
            field.setRequired(jsonObject.get("required").asBoolean());
        }

        if(jsonObject.hasKey("key")) {
            field.setKey(jsonObject.get("key").asBoolean());
        }

        if(jsonObject.hasKey("superProperties")) {
            JsonValue superPropertiesArray = jsonObject.get("superProperties");
            for (int i = 0; i < superPropertiesArray.length(); i++) {
                field.addSuperProperty(ResourceId.valueOf(superPropertiesArray.getString(i)));
            }
        }
        
        String type;
        JsonValue typeParameters;
        JsonValue typeElement = jsonObject.get("type");
        
        if(typeElement.isJsonPrimitive()) {
            type = typeElement.asString();
            typeParameters = jsonObject.get("typeParameters");
        } else {
            JsonValue typeObject = typeElement;
            type = typeObject.get("typeClass").asString();
            typeParameters = typeObject.get("parameters");
        }
        
        FieldTypeClass typeClass = TypeRegistry.get().getTypeClass(type);
        if(typeClass instanceof ParametrizedFieldTypeClass && !typeParameters.isJsonNull()) {
            field.setType(((ParametrizedFieldTypeClass) typeClass).deserializeType(typeParameters));
        } else {
            field.setType(typeClass.createType());
        }

        return field;
    }


}
