package org.activityinfo.ui.client.component.formdesigner.palette;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.ui.client.ClientContext;

import java.util.List;

public class Templates {


    public static List<Template> list() {
        List<Template> items = Lists.newArrayList();

        boolean newFields = ClientContext.isNewFieldsFlagEnabled();

        items.add(new TypeClassTemplate(SerialNumberType.TYPE_CLASS, I18N.CONSTANTS.serialNumber()));
        items.add(new TypeClassTemplate(QuantityType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeQuantity()));
        items.add(new TypeClassTemplate(TextType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeText()));
        items.add(new TypeClassTemplate(NarrativeType.TYPE_CLASS,  I18N.CONSTANTS.fieldTypeNarrative()));

        if(newFields) {
            items.add(new TypeClassTemplate(LocalDateType.TYPE_CLASS, I18N.CONSTANTS.date()));
        }

        items.add(new CheckboxTemplate());
        items.add(new RadioButtonTemplate());

        if(newFields) {
            items.add(new TypeClassTemplate(GeoPointType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeGeographicPoint()));
        }

        items.add(new TypeClassTemplate(BarcodeType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeBarcode()));
        items.add(new AttachmentFieldTemplate(AttachmentType.Kind.IMAGE, I18N.CONSTANTS.image()));
        items.add(new AttachmentFieldTemplate(AttachmentType.Kind.ATTACHMENT, I18N.CONSTANTS.attachment()));
        items.add(new TypeClassTemplate(CalculatedFieldType.TYPE_CLASS, I18N.CONSTANTS.fieldTypeCalculated()));

        if(newFields) {
            items.add(new SubFormTemplate(I18N.CONSTANTS.repeatingSubform(), SubFormKind.REPEATING));
            items.add(new SubFormTemplate(I18N.CONSTANTS.monthlySubform(), SubFormKind.MONTHLY));
            items.add(new SubFormTemplate(I18N.CONSTANTS.weeklySubform(), SubFormKind.WEEKLY));
            items.add(new SubFormTemplate(I18N.CONSTANTS.fortnightlySubform(), SubFormKind.BIWEEKLY));
            items.add(new SubFormTemplate(I18N.CONSTANTS.dailySubform(), SubFormKind.DAILY));

            items.add(new TypeClassTemplate(ReferenceType.TYPE_CLASS, I18N.CONSTANTS.reference()));
        }

        return items;
    }
}
