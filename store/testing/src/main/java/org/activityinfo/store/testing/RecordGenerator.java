/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.InputMask;
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
public class RecordGenerator implements Supplier<TypedFormRecord> {

    private Ids ids;
    private final FormClass schema;
    private Supplier<ResourceId> parentDistribution = null;
    private final Map<ResourceId, Supplier<FieldValue>> generators = new HashMap<>();
    private final Map<ResourceId, FormField> fieldMap = new HashMap<>();


    private int nextRecordIndex = 0;

    public RecordGenerator(FormClass schema) {
        this(new UnitTestingIds(), schema);
    }

    public RecordGenerator(Ids ids, FormClass schema) {
        this.ids = ids;
        this.schema = schema;

        for (Map.Entry<ResourceId, FieldValue> entry : ids.builtinValues().entrySet()) {
            generators.put(entry.getKey(), Suppliers.ofInstance(entry.getValue()));
        }

        for (FormField field : schema.getFields()) {
            fieldMap.put(field.getId(), field);

            if(!generators.containsKey(field.getId()) &&
                !(field.getType() instanceof CalculatedFieldType) &&
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
            EnumType enumType = (EnumType) field.getType();
            if(enumType.getCardinality() == Cardinality.SINGLE) {
                return new EnumGenerator(field, field.getId().hashCode());
            } else {
                return new MultiEnumGenerator(field);
            }
        } else if(field.getType() instanceof TextType) {
            TextType type = (TextType) field.getType();
            if(type.hasInputMask()) {
                return new InputMaskGenerator(new InputMask(type.getInputMask()), field.isRequired() ? 0 : 0.25, 0.10);
            } else {
                return new DiscreteTextGenerator(field.isRequired() ? 0 : 0.25, DiscreteTextGenerator.NAMES);
            }
        } else if(field.getType() instanceof LocalDateType) {
            return new DateGenerator(field);
        } else if(field.getType() instanceof SerialNumberType) {
            return new SerialNumberGenerator();
        } else if(field.getType() instanceof GeoPointType) {
            return new GeoPointGenerator(field);
        } else {
            return Suppliers.ofInstance(null);
        }
    }

    public RecordGenerator distribution(ResourceId fieldId, Supplier<FieldValue> distribution) {
        generators.put(fieldId, distribution);
        return this;
    }

    public RecordGenerator enumSeed(FormField field, int seed) {
        assert field.getType() instanceof EnumType;
        return distribution(field.getId(), new EnumGenerator(field, seed));
    }

    public List<TypedFormRecord> get(int rowCount) {
        List<TypedFormRecord> records = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            records.add(get());
        }
        return records;
    }

    @Override
    public TypedFormRecord get() {
        ResourceId recordId = ids.recordId(schema.getId(), nextRecordIndex++);
        TypedFormRecord record = new TypedFormRecord(recordId, schema.getId());

        for (Map.Entry<ResourceId, FieldValue> entry : ids.builtinValues().entrySet()) {
            record.set(entry.getKey(), entry.getValue());
        }

        if(parentDistribution != null) {
            record.setParentRecordId(parentDistribution.get());
        }

        for (Map.Entry<ResourceId, Supplier<FieldValue>> entry : generators.entrySet()) {
            record.set(entry.getKey(), entry.getValue().get());
        }
        return record;
    }
}
