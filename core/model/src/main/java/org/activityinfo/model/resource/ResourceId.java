package org.activityinfo.model.resource;


import org.activityinfo.model.form.annotation.Field;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.enumerated.EnumType;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * Globally, universally unique and persistent identifier
 * for {@code Resources}
 *
 */
public final class ResourceId {

    public static final ResourceId ROOT_ID = ResourceId.valueOf("_root");

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
        return new ResourceId(string);
    }

    public static ResourceId generateId() {
        return valueOf("c" + Long.toString(new Date().getTime(), Character.MAX_RADIX) +
                       Long.toString(COUNTER++, Character.MAX_RADIX));
    }


    private ResourceId(@Nonnull String text) {
        this.text = text;
    }

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
}
