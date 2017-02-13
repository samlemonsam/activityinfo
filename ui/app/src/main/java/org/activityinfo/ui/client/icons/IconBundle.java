package org.activityinfo.ui.client.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

public interface IconBundle extends ClientBundle {

    IconBundle INSTANCE = GWT.create(IconBundle.class);

    static ImageResource iconForField(FieldType type) {
        if(type instanceof QuantityType) {
            return INSTANCE.quantityField();

        } else if(type instanceof TextType) {
            return INSTANCE.textField();

        } else if(type instanceof LocalDateType) {
            return INSTANCE.dateField();

        } else if(type instanceof ReferenceType) {
            return INSTANCE.referenceField();

        } else if(type instanceof EnumType) {
            return INSTANCE.enumField();

        } else if(type instanceof GeoAreaType) {
            return INSTANCE.geoAreaField();

        } else {
            return INSTANCE.calculatedField();
        }
    }

    @Source("count.png")
    ImageResource count();

    @Source("field-quantity.png")
    ImageResource quantityField();

    @Source("field-calculated.png")
    ImageResource calculatedField();

    @Source("field-geoarea.png")
    ImageResource geoAreaField();

    @Source("field-text.png")
    ImageResource textField();

    @Source("field-enum.png")
    ImageResource enumField();

    @Source("field-reference.png")
    ImageResource referenceField();

    @Source("field-date.png")
    ImageResource dateField();

    @Source("form.png")
    ImageResource form();

    @Source("database-open.png")
    ImageResource databaseOpen();

    @Source("database-closed.png")
    ImageResource databaseClosed();
}
