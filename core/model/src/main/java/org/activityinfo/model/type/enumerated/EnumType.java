package org.activityinfo.model.type.enumerated;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.*;

import java.util.ArrayList;
import java.util.List;

public class EnumType implements ParametrizedFieldType {

    public interface EnumTypeClass extends ParametrizedFieldTypeClass, RecordFieldTypeClass { }

    public static final EnumTypeClass TYPE_CLASS = new EnumTypeClass() {

        @Override
        public String getId() {
            return "enumerated";
        }

        @Override
        public FieldType deserializeType(Record typeParameters) {

            Cardinality cardinality = Cardinality.valueOf(typeParameters.getString("cardinality"));

            List<EnumItem> enumItems = Lists.newArrayList();
            List<Record> enumValueRecords = typeParameters.getRecordList("values");
            for(Record record : enumValueRecords) {
                enumItems.add(EnumItem.fromRecord(record));
            }
            return new EnumType(cardinality, enumItems);
        }

        @Override
        public EnumType createType() {
            return new EnumType();
        }

        @Override
        public FormClass getParameterFormClass() {
            return new FormClass(ResourceIdPrefixType.TYPE.id("enum"));
        }

        @Override
        public EnumValue deserialize(Record record) {
            return EnumValue.fromRecord(record);
        }
    };

    private final Cardinality cardinality;
    private final List<EnumItem> values;
    private final List<EnumItem> defaultValues = Lists.newArrayList();

    public EnumType() {
        this.cardinality = Cardinality.SINGLE;
        this.values = Lists.newArrayList();
    }

    public EnumType(Cardinality cardinality, List<EnumItem> values) {
        this.cardinality = cardinality;
        this.values = values != null ? values : new ArrayList<EnumItem>();
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public List<EnumItem> getValues() {
        return values;
    }

    public List<EnumItem> getDefaultValues() {
        return defaultValues;
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public Record getParameters() {

        List<Record> enumValueRecords = Lists.newArrayList();
        for(EnumItem enumItem : getValues()) {
            enumValueRecords.add(enumItem.asRecord());
        }

        return new Record()
                .set("classId", getTypeClass().getParameterFormClass().getId())
                .set("cardinality", cardinality.name())
                .set("values", enumValueRecords);
    }

    @Override
    public boolean isValid() {
        return values.size() > 0;
    }

}
