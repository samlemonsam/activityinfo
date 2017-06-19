package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates random, but reproducible records for testing purposes.
 */
public class RecordGenerator {

    private final FormClass schema;
    private Supplier<ResourceId> parentDistribution = null;
    private final Map<ResourceId, Supplier<FieldValue>> generators = new HashMap<>();
    private final Map<ResourceId, FormField> fieldMap = new HashMap<>();

    private int nextRecordIndex = 0;

    public RecordGenerator(FormClass schema) {
        this.schema = schema;
        for (FormField field : schema.getFields()) {
            fieldMap.put(field.getId(), field);

            if(!(field.getType() instanceof CalculatedFieldType) &&
                !(field.getType() instanceof SubFormReferenceType)) {

                generators.put(field.getId(), generator(field));
            }
        }
    }

    public RecordGenerator parentForm(final TestForm parentForm) {
        parentDistribution = new ParentGenerator(parentForm);
        return this;
    }

    /**
     * Creates a basic, default field generator based only on the field definition.
     */
    private Supplier<FieldValue> generator(FormField field) {
        if(field.getType() instanceof QuantityType) {
            return new QuantityGenerator(field);
        } else if(field.getType() instanceof EnumType) {
            return new EnumGenerator(field);
        } else if(field.getType() instanceof TextType) {
            return new DiscreteTextGenerator(field.isRequired() ? 0 : 0.25, DiscreteTextGenerator.NAMES);
        } else if(field.getType() instanceof LocalDateType) {
            return new DateGenerator(field);
        } else if(field.getType() instanceof SerialNumberType) {
            return new SerialNumberGenerator();
        } else {
            return Suppliers.ofInstance(null);
        }
    }

    public RecordGenerator distribution(ResourceId fieldId, Supplier<FieldValue> distribution) {
        generators.put(fieldId, distribution);
        return this;
    }

    public List<FormInstance> generate(int rowCount) {
        List<FormInstance> records = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            records.add(generate());
        }
        return records;
    }

    public FormInstance generate() {
        ResourceId recordId = ResourceId.valueOf("c" + (nextRecordIndex++));
        FormInstance record = new FormInstance(recordId, schema.getId());

        if(parentDistribution != null) {
            record.setParentRecordId(parentDistribution.get());
        }

        for (Map.Entry<ResourceId, Supplier<FieldValue>> entry : generators.entrySet()) {
            record.set(entry.getKey(), entry.getValue().get());
        }
        return record;
    }
}
