package org.activityinfo.model.form;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The FormClass defines structure and semantics for {@code Resource}s.
 * <p/>
 * {@code Resources} which fulfill the contract described by a {@code FormClass}
 * are called {@code FormInstances}.
 */
public class FormClass implements FormElementContainer {


    public static final ResourceId PARENT_FIELD_ID = ResourceId.valueOf("@parent");


    @Nonnull
    private ResourceId id;

    private ResourceId databaseId;

    private String label;
    private String description;
    private final List<FormElement> elements = Lists.newArrayList();

    private ResourceId parentFormId = null;
    private SubFormKind subFormKind = null;

    private long schemaVersion;

    public FormClass(ResourceId id) {
        Preconditions.checkNotNull(id);
        this.id = id;
    }

    public ResourceId getDatabaseId() {
        return databaseId;
    }

    public FormClass setDatabaseId(ResourceId databaseId) {
        this.databaseId = databaseId;
        return this;
    }

    public FormClass setDatabaseId(int databaseId) {
        return setDatabaseId(CuidAdapter.databaseId(databaseId));
    }
    
    public FormElementContainer getElementContainer(ResourceId elementId) {
        return getElementContainerImpl(this, elementId);
    }

    private static FormElementContainer getElementContainerImpl(FormElementContainer container, final ResourceId elementId) {
        if (container.getId().equals(elementId)) {
            return container;
        }

        for (FormElement elem : container.getElements()) {
            if (elem instanceof FormElementContainer) {
                FormElementContainer result = getElementContainerImpl((FormElementContainer) elem, elementId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public FormElementContainer getParent(FormElement childElement) {
        return getContainerElementsImpl(this, childElement);
    }

    private static FormElementContainer getContainerElementsImpl(FormElementContainer container, final FormElement searchElement) {
        if (container.getElements().contains(searchElement)) {
            return container;
        }
        for (FormElement elem : container.getElements()) {
            if (elem instanceof FormElementContainer) {
                final FormElementContainer result = getContainerElementsImpl((FormElementContainer) elem, searchElement);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public void traverse(FormElementContainer element, TraverseFunction traverseFunction) {
        for (FormElement elem : Lists.newArrayList(element.getElements())) {
            traverseFunction.apply(elem, element);
            if (elem instanceof FormElementContainer) {
                traverse((FormElementContainer) elem, traverseFunction);
            }
        }
    }

    public void removeField(ResourceId formElementId) {
        remove(getField(formElementId));
    }

    public void remove(final FormElement formElement) {
        traverse(this, new TraverseFunction() {
            @Override
            public void apply(FormElement element, FormElementContainer container) {
                if (element.equals(formElement)) {
                    container.getElements().remove(formElement);
                }
            }
        });
    }

    @Override
    public ResourceId getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public FormClass setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * @return the version of this form schema
     */
    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getDescription() {
        return description;
    }

    public FormClass setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public List<FormElement> getElements() {
        return elements;
    }

    public Optional<FormElement> getElement(ResourceId elementId) {
        for (FormElement element : getAllElementsIncludingNested()) {
            if (element.getId().equals(elementId)) {
                return Optional.of(element);
            }
        }
        return Optional.absent();
    }

    public Set<FormElement> getAllElementsIncludingNested() {
        final Set<FormElement> result = Sets.newHashSet(elements);
        traverse(this, new TraverseFunction() {
            @Override
            public void apply(FormElement element, FormElementContainer container) {
                result.add(element);
            }
        });
        return result;
    }

    public List<FormField> getFields() {
        final List<FormField> fields = Lists.newArrayList();
        collectFields(fields, getElements(), new Predicate<FormElement>() {
            @Override
            public boolean apply(@Nullable FormElement input) {
                return input instanceof FormField;
            }
        });
        return fields;
    }

    public List<FormSection> getSections() {
        final List<FormSection> sections = Lists.newArrayList();
        collectFields(sections, getElements(), new Predicate<FormElement>() {
            @Override
            public boolean apply(@Nullable FormElement input) {
                return input instanceof FormSection;
            }
        });
        return sections;

    }

    private static void collectFields(List result, List<FormElement> elements, Predicate<FormElement> predicate) {
        for (FormElement element : elements) {

            if (predicate.apply(element)) {
                result.add(element);
            }

            if (element instanceof FormField) {
                // do nothing
            } else if (element instanceof FormSection) {
                final FormSection formSection = (FormSection) element;
                collectFields(result, formSection.getElements(), predicate);
            }
        }
    }

    public FormField getField(ResourceId fieldId) {
        for (FormField field : getFields()) {
            if (field.getId().equals(fieldId)) {
                return field;
            }
        }
        throw new IllegalArgumentException("No such field: " + fieldId);
    }

    @Override
    public FormClass addElement(FormElement element) {
        elements.add(element);
        return this;
    }

    public FormField addField() {
        return addField(ResourceId.generateId());
    }


    public FormField addField(ResourceId fieldId) {
        FormField field = new FormField(fieldId);
        elements.add(field);
        return field;
    }

    public FormClass insertElement(int index, FormElement element) {
        elements.add(index, element);
        return this;
    }

    public Optional<ResourceId> getParentFormId() {
        return Optional.fromNullable(parentFormId);
    }

    /**
     * Returns a {@code FormField} of type {@code ReferenceType} that will refer to the master submission
     * of a subform.
     * 
     * <p>If this {@code FormClass} is a sub form of another form, then each of its {@code FormSubmissions} will
     * have a {@code @parent} field value that refers back to the master form instance. There is no corresponding
     * {@code FormField} instance in this {@code FormClass}'s field list, because it is not a user-editable field.</p>
     */
    public Optional<FormField> getParentField() {
        Optional<ResourceId> parentFormId = getParentFormId();
        if(parentFormId.isPresent()) {
            FormField formField = new FormField(PARENT_FIELD_ID);
            formField.setLabel("Parent");
            formField.setType(ReferenceType.single(parentFormId.get()));
            return Optional.of(formField);
            
        } else {
            return Optional.absent();
        }
    }

    public FormClass setParentFormId(ResourceId parentFormId) {
        this.parentFormId = parentFormId;
        return this;
    }
    
    public boolean isSubForm() {
        return subFormKind != null;
    }

    public SubFormKind getSubFormKind() {
        if(!isSubForm()) {
            throw new IllegalStateException("Not a sub form");
        }
        return subFormKind;
    }

    public FormClass setSubFormKind(SubFormKind subFormKind) {
        this.subFormKind = subFormKind;
        return this;
    }

    @Override
    public String toString() {
        return "<FormClass: " + getLabel() + ">";
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id.asString());
        object.addProperty("schemaVersion", schemaVersion);
        
        if(databaseId != null) {
            object.addProperty("databaseId", databaseId.asString());
        }
        object.addProperty("label", label);
        
        
        if(!Strings.isNullOrEmpty(description)) {
            object.addProperty("description", description);
        }
        
        if(subFormKind != null) {
            object.addProperty("parentFormId", parentFormId.asString());
            object.addProperty("subFormKind", subFormKind.name().toLowerCase());
        }
        object.add("elements",  toJsonArray(elements));
        return object;
    }

    static JsonArray toJsonArray(Iterable<FormElement> elements) {
        JsonArray elementsArray = new JsonArray();
        for (FormElement element : elements) {
            elementsArray.add(element.toJsonObject());
        }
        return elementsArray;
    }

    public String toJsonString() {
        return toJsonObject().toString();
    }

    public static FormClass fromJson(JsonObject object) {
        // Deal with previous encoding

        ResourceId id;
        if(object.has("@id")) {
            id = ResourceId.valueOf(object.get("@id").getAsString());
        } else {
            id = ResourceId.valueOf(object.get("id").getAsString());
        }
        
        FormClass formClass = new FormClass(id);

        if(object.has("schemaVersion")) {
            formClass.setSchemaVersion(object.get("schemaVersion").getAsLong());
        }

        if(object.has("databaseId")) {
            formClass.setDatabaseId(ResourceId.valueOf(object.get("databaseId").getAsString()));
        }
        
        if(object.has("_class_label")) {
            formClass.setLabel(JsonParsing.toNullableString(object.get("_class_label")));
        } else {
            formClass.setLabel(JsonParsing.toNullableString(object.get("label")));
        }
        
        if(object.has("subFormKind")) {
            formClass.setSubFormKind(SubFormKind.valueOf(object.get("subFormKind").getAsString().toUpperCase()));
            formClass.setParentFormId(ResourceId.valueOf(object.get("parentFormId").getAsString()));
        }
        
        if(object.has("elements")) {
            JsonElement elements = object.get("elements");
            if(elements.isJsonArray()) {
                JsonArray elementsArray = elements.getAsJsonArray();
                formClass.elements.addAll(fromJsonArray(elementsArray));
            }
        }
        return formClass;
    }

    static List<FormElement> fromJsonArray(JsonArray elementsArray) {
        List<FormElement> elements = new ArrayList<>();
        for (int i = 0; i < elementsArray.size(); i++) {
            JsonObject elementObject = elementsArray.get(i).getAsJsonObject();
            elements.add(elementFromJson(elementObject));
        }
        return elements;
    }

    private static FormElement elementFromJson(JsonObject elementObject) {
        JsonElement typeElement = elementObject.get("type");
        if(typeElement.isJsonPrimitive()) {
            String type = typeElement.getAsString();
            if ("section".equals(type)) {
                return FormSection.fromJson(elementObject);
            } else if ("label".equals(type)) {
                return FormLabel.fromJson(elementObject);
            }
        }
        return FormField.fromJson(elementObject);
    }

    public static FormClass fromJson(String json) {
        return fromJson(new JsonParser().parse(json).getAsJsonObject());
    }
    
    public List<FormElement> getBuiltInElements() {
        List<FormElement> builtIn = Lists.newArrayList();
        for (FormElement elem : elements) {
            if (elem.getId().asString().startsWith(getId().asString())) {
                builtIn.add(elem);
            }
        }
        return builtIn;
    }
}
