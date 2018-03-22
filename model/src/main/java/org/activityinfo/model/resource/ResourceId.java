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
package org.activityinfo.model.resource;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.time.PeriodValue;
import org.codehaus.jackson.annotate.JsonValue;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Date;

/**
 * Globally, universally unique and persistent identifier
 * for {@code Resources}
 *
 */
@Schema(type = "string")
public final class ResourceId implements Serializable, JsonSerializable {

    public static final ResourceId ROOT_ID = ResourceId.valueOf("_root");

    public static final char GENERATED_ID_DOMAIN = 'c';
    public static final int RADIX = 10;
    public static long COUNTER = 1;

    private final String text;

    /**
     * Creates a new ResourceId from its string representation
     *
     * <p>Note: This method must be named {@code valueOf} in order to be
     * used as a Jersey {@code @PathParam}
     */
    public static ResourceId valueOf(@Nonnull String string) {
        assert string != null : "resourceid cannot be null";
        return new ResourceId(string);
    }

    /**
     * Generates a globally unique Id for a new collection
     */
    public static ResourceId generateId() {
        return valueOf(GENERATED_ID_DOMAIN + generateCuid());
    }

    public static ResourceId generatedPeriodSubmissionId(ResourceId rootInstanceId, String keyId) {
        return ResourceId.valueOf(rootInstanceId.asString() + "-" + keyId);
    }

    public static ResourceId periodSubRecordId(RecordRef parentRef, PeriodValue period) {
        return generatedPeriodSubmissionId(parentRef.getRecordId(), period.toString());
    }

    public static ResourceId generateSubmissionId(ResourceId collectionId) {
        switch (collectionId.getDomain()) {
            case CuidAdapter.ACTIVITY_DOMAIN:
                return CuidAdapter.generateSiteCuid();
            case CuidAdapter.LOCATION_TYPE_DOMAIN:
                return CuidAdapter.generateLocationCuid();
            case CuidAdapter.ADMIN_LEVEL_DOMAIN:
                return CuidAdapter.cuid(CuidAdapter.ADMIN_ENTITY_DOMAIN,  new KeyGenerator().generateInt());
            default:
                return ResourceId.valueOf(generateCuid());
        }
    }

    public static ResourceId generateSubmissionId(FormClass formClass) {
        return generateSubmissionId(formClass.getId());
    }

    public static String generateCuid() {
        return Long.toString(new Date().getTime(), Character.MAX_RADIX) +
                Long.toString(COUNTER++, Character.MAX_RADIX);
    }

    private ResourceId(@Nonnull String text) {
        this.text = text;
    }

    public static String malformedSubmissionId(ResourceId submissionId) {
        return "Invalid id: '" + submissionId.asString() + "'. Expected format: c{collectionId}-{submissionId}";
    }

    public static void checkSubmissionId(ResourceId submissionId) {
        Preconditions.checkArgument(submissionId.getDomain() == ResourceId.GENERATED_ID_DOMAIN, malformedSubmissionId(submissionId));

        String[] parts = submissionId.asString().split("-");
        Preconditions.checkArgument(parts.length >= 2, malformedSubmissionId(submissionId));
    }

    @JsonValue
    public String asString() {
        return this.text;
    }

    public char getDomain() {
        return text.charAt(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceId resourceId = (ResourceId) o;
        return text.equals(resourceId.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public String toString() {
        return text;
    }

    public static ResourceId generateFieldId(FieldTypeClass typeClass) {
        KeyGenerator generator = new KeyGenerator();
        if(typeClass == EnumType.TYPE_CLASS) {
            return CuidAdapter.attributeGroupField(generator.generateInt());
        } else {
            return CuidAdapter.indicatorField(generator.generateInt());
        }
    }

    public ResourceId fieldId(String propertyName) {
        return ResourceId.valueOf(text + "." + propertyName);
    }

    @Override
    public org.activityinfo.json.JsonValue toJson() {
        return Json.create(text);
    }

}
