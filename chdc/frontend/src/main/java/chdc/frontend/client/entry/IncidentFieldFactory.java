package chdc.frontend.client.entry;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;
import org.activityinfo.ui.client.input.view.InputHandler;
import org.activityinfo.ui.client.input.view.field.*;

import java.util.HashMap;
import java.util.Map;

import static chdc.frontend.client.entry.IncidentForm.*;

public class IncidentFieldFactory {

    private final Map<ResourceId, FieldWidget> fieldMap = new HashMap<>();
    private RecordRef recordRef;
    private InputHandler handler;

    public IncidentFieldFactory(RecordRef recordRef, InputHandler handler) {
        this.recordRef = recordRef;
        this.handler = handler;
    }

    public FieldWidget createWidget(FormField field) {
        return field.getType().accept(new FieldTypeVisitor<FieldWidget>() {
            @Override
            public FieldWidget visitAttachment(AttachmentType attachmentType) {
                return null;
            }

            @Override
            public FieldWidget visitCalculated(CalculatedFieldType calculatedFieldType) {
                return null;
            }

            @Override
            public FieldWidget visitReference(ReferenceType referenceType) {
                return null;
            }

            @Override
            public FieldWidget visitNarrative(NarrativeType narrativeType) {
                return new NarrativeWidget(new NarrativeAppearance(field.getLabel()), updater(field));
            }

            @Override
            public FieldWidget visitBoolean(BooleanType booleanType) {
                return null;
            }

            @Override
            public FieldWidget visitQuantity(QuantityType type) {
                return null;
            }

            @Override
            public FieldWidget visitGeoPoint(GeoPointType geoPointType) {
                return null;
            }

            @Override
            public FieldWidget visitGeoArea(GeoAreaType geoAreaType) {
                return null;
            }

            @Override
            public FieldWidget visitEnum(EnumType enumType) {
                if(enumType.getCardinality() == Cardinality.SINGLE &&
                        enumType.getEffectivePresentation() == EnumType.Presentation.RADIO_BUTTON) {
                    return new RadioGroupWidget(enumType, new RadioAppearance(field.getLabel()), updater(field));
                }

                return null;
            }

            @Override
            public FieldWidget visitBarcode(BarcodeType barcodeType) {
                return null;
            }

            @Override
            public FieldWidget visitSubForm(SubFormReferenceType subFormReferenceType) {
                return null;
            }

            @Override
            public FieldWidget visitLocalDate(LocalDateType localDateType) {
                return new IncidentDateWidget(field, updater(field));
            }

            @Override
            public FieldWidget visitMonth(MonthType monthType) {
                return null;
            }

            @Override
            public FieldWidget visitYear(YearType yearType) {
                return null;
            }

            @Override
            public FieldWidget visitFortnight(FortnightType fortnightType) {
                return null;
            }

            @Override
            public FieldWidget visitWeek(EpiWeekType epiWeekType) {
                return null;
            }

            @Override
            public FieldWidget visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
                return null;
            }

            @Override
            public FieldWidget visitText(TextType textType) {
                return new TextWidget(textType, new TextAppearance(field.getLabel()), updater(field));
            }

            @Override
            public FieldWidget visitSerialNumber(SerialNumberType serialNumberType) {
                return null;
            }
        });
    }

    private FieldUpdater updater(FormField field) {
        return input -> handler.updateModel(recordRef, field.getId(), input);
    }
}
