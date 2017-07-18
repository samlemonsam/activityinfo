package org.activityinfo.ui.client.table.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.HasStringValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;
import org.activityinfo.promise.Maybe;

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
        void renderTo(FieldValue fieldValue, SafeHtmlBuilder html);
    }

    private class TextValueRenderer implements ValueRenderer {

        @Override
        public void renderTo(FieldValue value, SafeHtmlBuilder html) {
            html.appendHtmlConstant("<p>");
            html.appendEscaped(((HasStringValue) value).asString());
            html.appendHtmlConstant("</p>");
        }
    }

    private class SerialNumberRenderer implements ValueRenderer {

        private SerialNumberType fieldType;

        public SerialNumberRenderer(SerialNumberType fieldType) {
            this.fieldType = fieldType;
        }

        @Override
        public void renderTo(FieldValue fieldValue, SafeHtmlBuilder html) {

            SerialNumber serialNumber = ((SerialNumber) fieldValue);
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
        public void renderTo(FieldValue fieldValue, SafeHtmlBuilder html) {
            html.appendHtmlConstant("<p>");
            html.append(((Quantity) fieldValue).getValue());
            html.appendHtmlConstant(" ");
            html.appendEscaped(type.getUnits());
            html.appendHtmlConstant("</p>");
        }
    }

    private class DateRenderer implements ValueRenderer {


        @Override
        public void renderTo(FieldValue fieldValue, SafeHtmlBuilder html) {

            LocalDate localDate = (LocalDate) fieldValue;

            html.appendHtmlConstant("<p>");
            html.appendEscaped(localDate.toString());
            html.appendHtmlConstant("</p>");
        }
    }

    private class EnumRenderer implements ValueRenderer {
        private EnumType type;

        public EnumRenderer(EnumType type) {
            this.type = type;
        }

        @Override
        public void renderTo(FieldValue fieldValue, SafeHtmlBuilder html) {
            EnumValue enumValue = (EnumValue) fieldValue;
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


    private class AttachmentRenderer implements ValueRenderer {

        @Override
        public void renderTo(FieldValue fieldValue, SafeHtmlBuilder html) {
            AttachmentValue attachments = (AttachmentValue) fieldValue;
            for (Attachment attachment : attachments.getValues()) {
                html.appendHtmlConstant("<p>");
                html.appendEscaped(attachment.getFilename());
                html.appendHtmlConstant("</p>");
            }
        }
    }

    private abstract class FieldRenderer {

        public abstract void renderTo(RecordTree recordTree, SafeHtmlBuilder html);
    }


    private class NullRenderer extends FieldRenderer {

        @Override
        public void renderTo(RecordTree recordTree, SafeHtmlBuilder html) {

        }
    }


    private class SimpleFieldRenderer extends FieldRenderer {
        private FormField field;
        private ValueRenderer valueRenderer;

        public SimpleFieldRenderer(FormField field, ValueRenderer renderer) {
            this.field = field;
            this.valueRenderer = renderer;
        }

        @Override
        public void renderTo(RecordTree recordTree, SafeHtmlBuilder html) {
            FieldValue fieldValue = recordTree.getRoot().get(field.getId());
            if(fieldValue != null) {
                html.appendHtmlConstant("<h3>");
                html.appendEscaped(field.getLabel());
                html.appendHtmlConstant("</h3>");
                valueRenderer.renderTo(fieldValue, html);
            }
        }
    }

    private class ReferenceFieldRenderer extends FieldRenderer {
        private final FormField field;
        private final LookupKeySet keySet;

        public ReferenceFieldRenderer(FormTree formTree, FormField field) {
            this.field = field;
            this.keySet = new LookupKeySet(formTree, field);
        }


        @Override
        public void renderTo(RecordTree recordTree, SafeHtmlBuilder html) {
            ReferenceValue fieldValue = (ReferenceValue) recordTree.getRoot().get(field.getId());
            if(fieldValue != null) {
                html.appendHtmlConstant("<h3>");
                html.appendEscaped(field.getLabel());
                html.appendHtmlConstant("</h3>");

                for (RecordRef recordRef : fieldValue.getReferences()) {
                    Maybe<String> label = keySet.label(recordTree, recordRef);
                    if(label.isVisible()) {
                        html.appendEscaped(label.get());
                    }
                }
            }
        }
    }


    private final List<FieldRenderer> renderers = new ArrayList<>();

    public DetailsRenderer(FormTree tree) {
        if(tree.getRootState() == FormTree.State.VALID) {
            FormClass formClass = tree.getRootFormClass();
            for (FormField field : formClass.getFields()) {
                renderers.add(buildRenderer(tree, field));
            }
        }
    }

    private FieldRenderer buildRenderer(FormTree formTree, FormField field) {
        return field.getType().accept(new FieldTypeVisitor<FieldRenderer>() {
            @Override
            public FieldRenderer visitAttachment(AttachmentType attachmentType) {
                return new SimpleFieldRenderer(field, new AttachmentRenderer());
            }

            @Override
            public FieldRenderer visitCalculated(CalculatedFieldType calculatedFieldType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitReference(ReferenceType referenceType) {
                return new ReferenceFieldRenderer(formTree, field);
            }

            @Override
            public FieldRenderer visitNarrative(NarrativeType narrativeType) {
                return new SimpleFieldRenderer(field, new TextValueRenderer());
            }

            @Override
            public FieldRenderer visitBoolean(BooleanType booleanType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitQuantity(QuantityType type) {
                return new SimpleFieldRenderer(field, new QuantityRenderer(type));
            }

            @Override
            public FieldRenderer visitGeoPoint(GeoPointType geoPointType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitGeoArea(GeoAreaType geoAreaType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitEnum(EnumType enumType) {
                return new SimpleFieldRenderer(field, new EnumRenderer(enumType));
            }

            @Override
            public FieldRenderer visitBarcode(BarcodeType barcodeType) {
                return new SimpleFieldRenderer(field, new TextValueRenderer());
            }

            @Override
            public FieldRenderer visitSubForm(SubFormReferenceType subFormReferenceType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitLocalDate(LocalDateType localDateType) {
                return new SimpleFieldRenderer(field, new DateRenderer());
            }

            @Override
            public FieldRenderer visitMonth(MonthType monthType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitYear(YearType yearType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
                return new NullRenderer();
            }

            @Override
            public FieldRenderer visitText(TextType textType) {
                return new SimpleFieldRenderer(field, new TextValueRenderer());
            }

            @Override
            public FieldRenderer visitSerialNumber(SerialNumberType serialNumberType) {
                return new SimpleFieldRenderer(field, new SerialNumberRenderer(serialNumberType));
            }
        });
    }

    public SafeHtml render(RecordTree record) {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        for (FieldRenderer renderer : renderers) {
            renderer.renderTo(record, html);
        }
        return html.toSafeHtml();
    }

    public SafeHtml renderNoSelection() {
        return SafeHtmlUtils.fromTrustedString("No selection.");
    }




}
