package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

/**
 * An empty form of null values for certain field types
 */
public class EmptyForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("EMPTY_FORM");

    public static final ResourceId TEXT_FIELD_ID = ResourceId.valueOf("F1");
    public static final ResourceId QUANTITY_FIELD_ID = ResourceId.valueOf("F2");
    public static final ResourceId ENUM_FIELD_ID = ResourceId.valueOf("F3");
    public static final ResourceId POP_ENUM_FIELD_ID = ResourceId.valueOf("F4");

    public static final ResourceId ENUM_ONE_ID = ResourceId.valueOf("E1");
    public static final ResourceId ENUM_TWO_ID = ResourceId.valueOf("E2");

    public static final int ROW_COUNT = 5;

    private final FormClass formClass;
    private final FormField textField;
    private final FormField quantityField;
    private final FormField enumField;
    private final FormField popEnumField;

    private List<FormInstance> records;

    public EmptyForm() {
        formClass = new FormClass(FORM_ID)
                .setLabel("Empty Form");

        textField = formClass.addField(TEXT_FIELD_ID)
                .setCode("TEXT")
                .setLabel("Null Text")
                .setRequired(false)
                .setType(TextType.SIMPLE);

        quantityField = formClass.addField(QUANTITY_FIELD_ID)
                .setCode("QUANT")
                .setLabel("Null Quantity")
                .setRequired(false)
                .setType(new QuantityType(null));

        enumField = formClass.addField(ENUM_FIELD_ID)
                .setCode("ENUM")
                .setLabel("Null Enumeration")
                .setRequired(false)
                .setType(new EnumType());

        popEnumField = formClass.addField(POP_ENUM_FIELD_ID)
                .setCode("POPENUM")
                .setLabel("Populated Enumeration")
                .setRequired(false)
                .setType(new EnumType(Cardinality.SINGLE,
                            new EnumItem(ENUM_ONE_ID,"One"),
                            new EnumItem(ENUM_TWO_ID,"Two")));
    }

    @Override
    public ResourceId getFormId() {
        return FORM_ID;
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    @Override
    public List<FormInstance> getRecords() {
        if(records == null) {
            this.records = new RecordGenerator(formClass)
                    .distribution(TEXT_FIELD_ID, new DiscreteTextGenerator(1.0,""))
                    .distribution(QUANTITY_FIELD_ID, new IntegerGenerator(0,1,1.0,null))
                    .distribution(ENUM_FIELD_ID, new EmptyEnumGenerator())
                    .distribution(POP_ENUM_FIELD_ID, new EnumGenerator(popEnumField))
                    .generate(ROW_COUNT);
        }
        return records;
    }
}
