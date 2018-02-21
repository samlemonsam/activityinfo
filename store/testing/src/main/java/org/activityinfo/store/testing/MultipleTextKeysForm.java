package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

public class MultipleTextKeysForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("MULT_TEXT_KEYS");

    public static final ResourceId FIRST_TEXT_KEY_ID = ResourceId.valueOf("F1");
    public static final ResourceId SECOND_TEXT_KEY_ID = ResourceId.valueOf("F2");

    private static final String[] NAMES = {"One", "Two", "Three", "Four", "Five"};
    private static final String[] TYPES = {"Type One", "Type Two", "Type Three", "Type Four", "Type Five"};

    public static final int ROW_COUNT = 5;

    private final FormClass formClass;
    private final FormField firstTextKey;
    private final FormField secondTextKey;

    private List<FormInstance> records;
    private RecordGenerator generator;

    public MultipleTextKeysForm(Ids ids) {
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Multiple Text Key Fields Form");
        formClass.setDatabaseId(ids.databaseId());

        firstTextKey = formClass.addField(FIRST_TEXT_KEY_ID)
                .setCode("ONE")
                .setLabel("First Text Key")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setVisible(true)
                .setKey(true);

        secondTextKey = formClass.addField(SECOND_TEXT_KEY_ID)
                .setCode("TWO")
                .setLabel("Second Text Key")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setVisible(true)
                .setKey(true);

        generator = new RecordGenerator(formClass)
                .distribution(FIRST_TEXT_KEY_ID, new DiscreteTextGenerator(0.0, NAMES))
                .distribution(SECOND_TEXT_KEY_ID, new DiscreteTextGenerator(0.0, TYPES));
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
        if (records == null) {
            this.records = generator.get(ROW_COUNT);
        }
        return records;
    }

    @Override
    public Supplier<FormInstance> getGenerator() {
        return generator;
    }

    public FormField getFirstTextKey() {
        return firstTextKey;
    }

    public FormField getSecondTextKey() {
        return secondTextKey;
    }
}
