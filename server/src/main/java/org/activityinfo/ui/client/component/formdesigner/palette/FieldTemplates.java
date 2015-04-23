package org.activityinfo.ui.client.component.formdesigner.palette;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

public class FieldTemplates {


    public static List<FieldTemplate> list() {
        List<FieldTemplate> items = Lists.newArrayList();

        // use only types supported by backend for the moment

//        // Add all type classes to the palette except for reference + enum types:
//        // we will handle those specially
//        for(FieldTypeClass typeClass : TypeRegistry.get().getTypeClasses()) {
//            if(typeClass != ReferenceType.TypeClass.INSTANCE &&
//               typeClass != EnumType.TypeClass.INSTANCE) {
//                items.add(new TypeClassTemplate(typeClass));
//            }
//        }
//
//        // ReferenceTypes are a bit abstract, we will provide a number of
//        // concrete types that make will hopefully make sense to the user

        items.add(new TypeClassTemplate(QuantityType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeQuantity()));
        items.add(new TypeClassTemplate(TextType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeText()));
        items.add(new TypeClassTemplate(NarrativeType.TYPE_CLASS,  I18N.CONSTANTS.fieldTypeNarrative()));
  //      items.add(new TypeClassTemplate(LocalDateType.TYPE_CLASS));
  //      items.add(new TypeClassTemplate(LocalDateIntervalType.TYPE_CLASS));

        items.add(new CheckboxTemplate());
        items.add(new RadioButtonTemplate());

    //    items.add(new TypeClassTemplate(GeoPointType.TYPE_CLASS));
        items.add(new TypeClassTemplate(BarcodeType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeBarcode()));
 //       items.add(new TypeClassTemplate(ImageType.TYPE_CLASS));
        items.add(new TypeClassTemplate(CalculatedFieldType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeCalculated()));

        return items;
    }
}
