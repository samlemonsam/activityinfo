package org.activityinfo.ui.client.table.view;

import com.google.gson.JsonElement;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;

import java.util.ArrayList;
import java.util.List;

public class DetailsRenderer {

    private static int id = 1;

    interface Templates extends SafeHtmlTemplates {
        @Template("<h3 class=\"{0}\">{1}</h3>")
        SafeHtml fieldLabel(String className, String label);
    }

    private final static Templates TEMPLATES = GWT.create(Templates.class);

    private interface ValueRenderer {
        void renderTo(JsonElement fieldValue, SafeHtmlBuilder html);
    }

    private class TextValueRenderer implements ValueRenderer {

        @Override
        public void renderTo(JsonElement value, SafeHtmlBuilder html) {
            html.appendHtmlConstant("<p>");
            html.appendEscaped(value.getAsString());
            html.appendHtmlConstant("</p>");
        }
    }

    private class SerialNumberRenderer implements ValueRenderer {

        private SerialNumberType fieldType;

        public SerialNumberRenderer(SerialNumberType fieldType) {
            this.fieldType = fieldType;
        }

        @Override
        public void renderTo(JsonElement fieldValue, SafeHtmlBuilder html) {

            SerialNumber serialNumber = fieldType.parseJsonValue(fieldValue);
            String serial = fieldType.format(serialNumber);

            html.appendHtmlConstant("<p>");
            html.appendEscaped(serial);
            html.appendHtmlConstant("</p>");
        }
    }

    private class QuantityRenderer implements ValueRenderer {

        private QuantityType type;

        public QuantityRenderer(QuantityType type) {
            this.type = type;
        }

        @Override
        public void renderTo(JsonElement fieldValue, SafeHtmlBuilder html) {
            html.appendHtmlConstant("<p>");
            html.append(fieldValue.getAsDouble());
            html.appendHtmlConstant(" ");
            html.appendEscaped(type.getUnits());
            html.appendHtmlConstant("</p>");
        }
    }

    private class DateRenderer implements ValueRenderer {


        @Override
        public void renderTo(JsonElement fieldValue, SafeHtmlBuilder html) {

            LocalDate localDate = LocalDateType.INSTANCE.parseJsonValue(fieldValue);

            html.appendHtmlConstant("<p>");
            html.appendEscaped(localDate.toString());
            html.appendHtmlConstant("</p>");
        }
    }

    private class NullRenderer implements ValueRenderer {
        @Override
        public void renderTo(JsonElement fieldValue, SafeHtmlBuilder html) {

        }
    }

    private class EnumRenderer implements ValueRenderer {
        private EnumType type;

        public EnumRenderer(EnumType type) {
            this.type = type;
        }

        @Override
        public void renderTo(JsonElement fieldValue, SafeHtmlBuilder html) {
            EnumValue enumValue = type.parseJsonValue(fieldValue);
            html.appendHtmlConstant("<p>");

            boolean needsComma = false;
            for (EnumItem item : type.getValues()) {
                if(enumValue.getResourceIds().contains(item.getId())) {
                    if(needsComma) {
                        html.appendEscaped(", ");
                    }
                    html.appendEscaped(item.getLabel());
                    needsComma = true;
                }
            }
            html.appendHtmlConstant("</p>");

        }
    }

    private class FieldRenderer {
        private FormField field;
        private ValueRenderer valueRenderer;

        public FieldRenderer(FormField field, ValueRenderer renderer) {
            this.field = field;
            this.valueRenderer = renderer;
        }

        public void renderTo(FormRecord record, SafeHtmlBuilder html) {
            JsonElement fieldValue = record.getFields().get(field.getName());
            if(fieldValue != null) {
                html.appendHtmlConstant("<h3>");
                html.appendEscaped(field.getLabel());
                html.appendHtmlConstant("</h3>");
                valueRenderer.renderTo(fieldValue, html);
            }
        }
    }


    private final List<FieldRenderer> renderers = new ArrayList<>();

    public DetailsRenderer(FormTree tree) {
        if(tree.getRootState() == FormTree.State.VALID) {
            FormClass formClass = tree.getRootFormClass();
            for (FormField field : formClass.getFields()) {
                renderers.add(new FieldRenderer(field, buildRenderer(field.getType())));
            }
        }
    }

    private ValueRenderer buildRenderer(FieldType type) {
        return type.accept(new FieldTypeVisitor<ValueRenderer>() {
            @Override
            public ValueRenderer visitAttachment(AttachmentType attachmentType) {
                // TODO
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitCalculated(CalculatedFieldType calculatedFieldType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitReference(ReferenceType referenceType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitNarrative(NarrativeType narrativeType) {
                return new TextValueRenderer();
            }

            @Override
            public ValueRenderer visitBoolean(BooleanType booleanType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitQuantity(QuantityType type) {
            return new QuantityRenderer(type);
            }

            @Override
            public ValueRenderer visitGeoPoint(GeoPointType geoPointType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitGeoArea(GeoAreaType geoAreaType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitEnum(EnumType enumType) {
                return new EnumRenderer((EnumType) type);
            }

            @Override
            public ValueRenderer visitBarcode(BarcodeType barcodeType) {
                return new TextValueRenderer();
            }

            @Override
            public ValueRenderer visitSubForm(SubFormReferenceType subFormReferenceType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitLocalDate(LocalDateType localDateType) {
                return new DateRenderer();
            }

            @Override
            public ValueRenderer visitMonth(MonthType monthType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitYear(YearType yearType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
                return new NullRenderer();
            }

            @Override
            public ValueRenderer visitText(TextType textType) {
                return new TextValueRenderer();
            }

            @Override
            public ValueRenderer visitSerialNumber(SerialNumberType serialNumberType) {
                return new SerialNumberRenderer(serialNumberType);
            }
        });
    }

    public SafeHtml render(FormRecord formRecord) {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        for (FieldRenderer renderer : renderers) {
            renderer.renderTo(formRecord, html);
        }
        return html.toSafeHtml();
    }

    public SafeHtml renderNoSelection() {
        return SafeHtmlUtils.fromTrustedString("No selection.");
    }




}
