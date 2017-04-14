package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.inject.Inject;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.Item;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.InputMask;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDateIntervalType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.type.time.MonthType;
import org.activityinfo.model.type.time.YearType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class OdkFormFieldBuilderFactory {

    public static final Escaper REGEX_ESCAPER = Escapers.builder().addEscape('\'', "\\'").build();

    private static final Logger LOGGER = Logger.getLogger(OdkFormFieldBuilderFactory.class.getName());

    final private ResourceLocatorSync locator;

    @Inject
    public OdkFormFieldBuilderFactory(ResourceLocatorSync table) {
        this.locator = table;
    }

    public OdkFormFieldBuilder get(FieldType fieldType) {
        if (fieldType instanceof ParametrizedFieldType) {
            ParametrizedFieldType parametrizedFieldType = (ParametrizedFieldType) fieldType;
            if (!parametrizedFieldType.isValid()) {
                return OdkFormFieldBuilder.NONE;
            }
        }

        return fieldType.accept(new FieldTypeVisitor<OdkFormFieldBuilder>() {

            @Override
            public OdkFormFieldBuilder visitAttachment(AttachmentType attachmentType) {
                return new UploadBuilder("image/*");
            }

            @Override
            public OdkFormFieldBuilder visitCalculated(CalculatedFieldType calculatedFieldType) {
                return OdkFormFieldBuilder.NONE;
            }

            @Override
            public OdkFormFieldBuilder visitReference(ReferenceType referenceType) {
                if(isSmallSet(referenceType.getRange())) {
                    return new SelectBuilder(BindingType.STRING, referenceOptions(referenceType));
                } else {
                    return new ReferenceBuilder(referenceType.getRange());
                }
            }

            @Override
            public OdkFormFieldBuilder visitNarrative(NarrativeType narrativeType) {
                return new SimpleInputBuilder(BindingType.STRING);
            }

            @Override
            public OdkFormFieldBuilder visitBoolean(BooleanType booleanType) {
                return new SelectBuilder(BindingType.BOOLEAN, booleanOptions());
            }

            @Override
            public OdkFormFieldBuilder visitQuantity(QuantityType type) {
                return new QuantityFieldBuilder(type);
            }

            @Override
            public OdkFormFieldBuilder visitGeoPoint(GeoPointType geoPointType) {
                return new SimpleInputBuilder(BindingType.GEOPOINT);
            }

            @Override
            public OdkFormFieldBuilder visitGeoArea(GeoAreaType geoAreaType) {
                return OdkFormFieldBuilder.NONE;
            }

            @Override
            public OdkFormFieldBuilder visitEnum(EnumType enumType) {
                SelectOptions options = enumOptions(enumType);
                if(options.isEmpty()) {
                    return OdkFormFieldBuilder.NONE;
                } else {
                    return new SelectBuilder(BindingType.STRING, options);
                }
            }

            @Override
            public OdkFormFieldBuilder visitBarcode(BarcodeType barcodeType) {
                return new SimpleInputBuilder(BindingType.BARCODE);
            }

            @Override
            public OdkFormFieldBuilder visitSubForm(SubFormReferenceType subFormReferenceType) {
                return OdkFormFieldBuilder.NONE;
            }

            @Override
            public OdkFormFieldBuilder visitLocalDate(LocalDateType localDateType) {
                return new SimpleInputBuilder(BindingType.DATE);
            }

            @Override
            public OdkFormFieldBuilder visitMonth(MonthType monthType) {
                return OdkFormFieldBuilder.NONE;
            }

            @Override
            public OdkFormFieldBuilder visitYear(YearType yearType) {
                return OdkFormFieldBuilder.NONE;
            }

            @Override
            public OdkFormFieldBuilder visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
                return OdkFormFieldBuilder.NONE;
            }

            @Override
            public OdkFormFieldBuilder visitText(TextType textType) {
                return new SimpleInputBuilder(BindingType.STRING, textConstraint(textType));
            }

            private Optional<String> textConstraint(TextType textType) {
                if(textType.hasInputMask()) {
                    InputMask inputMask = new InputMask(textType.getInputMask());
                    return Optional.of(String.format("regex(., '%s')",
                            REGEX_ESCAPER.escape(inputMask.toXFormRegex())));

                } else {
                    return Optional.absent();
                }
            }

            @Override
            public OdkFormFieldBuilder visitSerialNumber(SerialNumberType serialNumberType) {
                return OdkFormFieldBuilder.NONE;
            }
        });
    }

    private boolean isSmallSet(Collection<ResourceId> range) {
        // TODO: hardcoded to include partner/projects in form
        if(range.size() == 1) {
            ResourceId formClassId = Iterables.getOnlyElement(range);
            if(formClassId.getDomain() == CuidAdapter.PARTNER_FORM_CLASS_DOMAIN ||
               formClassId.getDomain() == CuidAdapter.PROJECT_CLASS_DOMAIN) {
                return true;
            }
        }
        return false;
    }

    private SelectOptions enumOptions(EnumType enumType) {
        Cardinality cardinality = enumType.getCardinality();
        List<Item> items = Lists.newArrayListWithCapacity(enumType.getValues().size());
        for (EnumItem enumItem : enumType.getValues()) {
            Item item = new Item();
            item.setLabel(enumItem.getLabel());
            item.setValue(enumItem.getId().asString());
            items.add(item);
        }
        return new SelectOptions(cardinality, items);
    }

    private SelectOptions booleanOptions() {
        Item no = new Item();
        no.setLabel("no");
        no.setValue("FALSE");

        Item yes = new Item();
        yes.setLabel("yes");
        yes.setValue("TRUE");

        return new SelectOptions(Cardinality.SINGLE, Arrays.asList(yes, no));
    }

    private SelectOptions referenceOptions(ReferenceType referenceType) {
        ArrayList<Item> items = Lists.newArrayList();

        for (ReferenceChoice choice : locator.getReferenceChoices(referenceType.getRange())) {
            Item item = new Item();
            item.setLabel(choice.getLabel());
            item.setValue(choice.getRef().toQualifiedString());
            items.add(item);
        }

        return new SelectOptions(referenceType.getCardinality(), items);
    }
}
