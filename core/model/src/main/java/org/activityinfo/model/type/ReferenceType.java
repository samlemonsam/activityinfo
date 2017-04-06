package org.activityinfo.model.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;

import java.util.*;

/**
 * A type that represents a link or reference to another {@code Resource}
 */
public class ReferenceType implements ParametrizedFieldType {

    public static class TypeClass implements ParametrizedFieldTypeClass, RecordFieldTypeClass {

        private TypeClass() {
        }

        @Override
        public String getId() {
            return "reference";
        }

        @Override
        public FieldType createType() {
            return new ReferenceType()
                    .setCardinality(Cardinality.SINGLE)
                    .setRange(Collections.<ResourceId>emptySet());
        }

        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            
            List<ResourceId> range = new ArrayList<>();
            JsonArray rangeArray = parametersObject.get("range").getAsJsonArray();
            for (JsonElement rangeElement : rangeArray) {
                String formId;
                if(rangeElement.isJsonPrimitive()) {
                    formId = rangeElement.getAsString();                    
                } else {
                    formId = rangeElement.getAsJsonObject().get("formId").getAsString();
                }
                range.add(ResourceId.valueOf(formId));
            }
            
            ReferenceType type = new ReferenceType();
            type.setCardinality(Cardinality.valueOf(parametersObject.get("cardinality")));    
            type.setRange(range);
            return type;
        }
    }

    public static final TypeClass TYPE_CLASS = new TypeClass();

    private Cardinality cardinality;
    private final List<ResourceId> range = Lists.newArrayList();

    public ReferenceType() {
    }

    public ReferenceType(Cardinality cardinality, ResourceId rangeFormId) {
        this.cardinality = cardinality;
        this.range.add(rangeFormId);
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {

        if(value.isJsonNull()) {
            return new ReferenceValue();
        } else if(value.isJsonArray()) {
            JsonArray array = (JsonArray) value;
            Set<RecordRef> refs = new HashSet<>();
            for (JsonElement jsonElement : array) {
                refs.add(parseRef(jsonElement.getAsString()));
            }
            return new ReferenceValue(refs);
        } else {
            return new ReferenceValue(parseRef(value.getAsString()));
        }
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitReference(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    private RecordRef parseRef(String ref) {
        int separator = ref.indexOf(':');
        if(separator == -1) {
            ResourceId formId = Iterables.getOnlyElement(range);
            return new RecordRef(formId, ResourceId.valueOf(ref));
        } else {
            return RecordRef.fromQualifiedString(ref);
        }
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public ReferenceType setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
        return this;
    }

    /**
     * @return the set of FormClasses to which fields of this type can refer.
     */
    public Collection<ResourceId> getRange() {
        return range;
    }


    public ReferenceType setRange(ResourceId formClassId) {
        this.range.clear();
        this.range.add(formClassId);
        return this;
    }

    public ReferenceType setRange(Collection<ResourceId> range) {
        this.range.clear();
        this.range.addAll(range);
        return this;
    }

    @Override
    public JsonObject getParametersAsJson() {
        JsonObject object = new JsonObject();
        object.addProperty("cardinality", cardinality.name().toLowerCase());
        
        JsonArray rangeArray = new JsonArray();
        for (ResourceId formId : range) {
            JsonObject rangeObject = new JsonObject();
            rangeObject.addProperty("formId", formId.asString());
            rangeArray.add(rangeObject);
        }
        object.add("range", rangeArray);
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private List<String> toArray(Set<ResourceId> range) {
        List<String> ids = Lists.newArrayList();
        for(ResourceId id : range) {
            ids.add(id.asString());
        }
        return ids;
    }


    /**
     * Convenience constructor for ReferenceTypes with single cardinality
     * @param formClassId the id of the form class which is the range of this field
     * @return a new ReferenceType
     */
    public static ReferenceType single(ResourceId formClassId) {
        ReferenceType type = new ReferenceType();
        type.setCardinality(Cardinality.SINGLE);
        type.setRange(Collections.singleton(formClassId));
        return type;
    }

    /**
     * Convenience constructor for ReferenceTypes with single cardinality
     * @param formClassIds the ids of the form class which constitute the range of this field
     * @return a new ReferenceType
     */
    public static ReferenceType single(Iterable<ResourceId> formClassIds) {
        ReferenceType type = new ReferenceType();
        type.setCardinality(Cardinality.SINGLE);
        type.setRange(Sets.newHashSet(formClassIds));
        return type;
    }

    public static FieldType multiple(Collection<ResourceId> formClassIds) {
        ReferenceType type = new ReferenceType();
        type.setCardinality(Cardinality.MULTIPLE);
        type.setRange(Sets.newHashSet(formClassIds));
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReferenceType that = (ReferenceType) o;

        if (cardinality != that.cardinality) {
            return false;
        }
        if (!range.equals(that.range)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = cardinality.hashCode();
        result = 31 * result + range.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ReferenceType{" +
               "cardinality=" + cardinality +
               ", range=" + range +
               '}';
    }
}
