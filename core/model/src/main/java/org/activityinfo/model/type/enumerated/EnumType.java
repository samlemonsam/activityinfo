package org.activityinfo.model.type.enumerated;

import com.google.common.collect.Lists;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;

import java.util.*;

import static org.activityinfo.json.Json.createObject;

public class EnumType implements ParametrizedFieldType {

    /**
     * The maximum number of items to show as checkboxes, when presentation is "Automatic". Above
     * this count, dropdown will be used.
     */
    public static final int MAX_CHECKBOX_ITEMS = 10;

    public interface EnumTypeClass extends ParametrizedFieldTypeClass, RecordFieldTypeClass {
        @Override
        EnumType deserializeType(org.activityinfo.json.JsonObject parametersObject);
    }

    public enum Presentation {
        AUTOMATIC,
        RADIO_BUTTON,
        DROPDOWN
    }

    public static final EnumTypeClass TYPE_CLASS = new EnumTypeClass() {

        @Override
        public String getId() {
            return "enumerated";
        }


        @Override
        public EnumType deserializeType(org.activityinfo.json.JsonObject parametersObject) {
            // Explicit type parameter required by GWT's compiler!
            Cardinality cardinality = Cardinality.valueOf(
                    parametersObject.<JsonValue>get("cardinality"));

            Presentation presentation = Presentation.AUTOMATIC;
            if(parametersObject.hasKey("presentation")) {
                String presentationType = parametersObject.get("presentation").asString().toUpperCase();
                switch (presentationType) {
                    case "CHECKBOX":
                    case "RADIO_BUTTON":
                        presentation = Presentation.RADIO_BUTTON;
                        break;
                    case "DROPDOWN":
                        presentation = Presentation.DROPDOWN;
                        break;
                    default:
                        presentation = Presentation.AUTOMATIC;
                        break;
                }
            }

            List<EnumItem> enumItems = Lists.newArrayList();
            JsonValue valuesArray = parametersObject.get("values");
            if(valuesArray != null) {
                if (valuesArray.isJsonArray()) {
                    JsonArray enumItemArray = valuesArray.getAsJsonArray();
                    for (JsonValue record : enumItemArray.values()) {
                        enumItems.add(EnumItem.fromJsonObject(record.getAsJsonObject()));
                    }
                }
            }
            return new EnumType(cardinality, presentation, enumItems);
        }

        @Override
        public EnumType createType() {
            return new EnumType();
        }

    };

    private final Cardinality cardinality;
    private final List<EnumItem> values;
    private final List<EnumItem> defaultValues = Lists.newArrayList();
    private final Presentation presentation;

    public EnumType() {
        this(Cardinality.SINGLE, Collections.<EnumItem>emptyList());
    }

    public EnumType(Cardinality cardinality, List<EnumItem> values) {
        this(cardinality, Presentation.AUTOMATIC, values);
    }


    public EnumType(Cardinality cardinality, EnumItem... values) {
        this(cardinality, Arrays.asList(values));
    }

    public EnumType(Cardinality cardinality, Presentation presentation, List<EnumItem> values) {
        this.cardinality = cardinality;
        this.values = values != null ? values : new ArrayList<EnumItem>();
        this.presentation = presentation;
    }

    public EnumType withPresentation(Presentation presentation) {
        return new EnumType(cardinality, presentation, values);
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public Presentation getPresentation() {
        return presentation;
    }

    public Presentation getEffectivePresentation() {
        if(presentation == Presentation.AUTOMATIC) {
            if(values.size() > MAX_CHECKBOX_ITEMS) {
                return Presentation.DROPDOWN;
            } else {
                return Presentation.RADIO_BUTTON;
            }
        } else {
            return presentation;
        }
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
    public EnumValue parseJsonValue(JsonValue value) {
        if(value.isJsonString()) {
            ResourceId id = ResourceId.valueOf(value.asString());
            return new EnumValue(id);
        } else if(value.isJsonArray()) {
            Set<ResourceId> ids = new HashSet<>();
            JsonArray array = value.getAsJsonArray();
            for (int i = 0; i < array.length(); i++) {
                ResourceId id = ResourceId.valueOf(array.get(i).asString());
                ids.add(id);
            }
            return new EnumValue(ids);

        } else {
            return null;
        }
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitEnum(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public JsonObject getParametersAsJson() {
        
        JsonArray enumValueArray = Json.createArray();
        for (EnumItem enumItem : getValues()) {
            enumValueArray.add(enumItem.toJsonObject());
        }

        JsonObject object = createObject();
        object.put("cardinality", cardinality.name().toLowerCase());
        object.put("presentation", presentation.name().toLowerCase());
        object.put("values", enumValueArray);
        return object;
    }

    @Override
    public boolean isValid() {
        return values.size() > 0;
    }

}
