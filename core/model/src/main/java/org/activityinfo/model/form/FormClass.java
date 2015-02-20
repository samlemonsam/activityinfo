package org.activityinfo.model.form;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.model.resource.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * The FormClass defines structure and semantics for {@code Resource}s.
 * <p/>
 * {@code Resources} which fulfill the contract described by a {@code FormClass}
 * are called {@code FormInstances}.
 */
public class FormClass implements IsResource, FormElementContainer {


    /**
     * Because FormClasses are themselves FormInstances, they have a class id of their own
     */
    public static final ResourceId CLASS_ID = ResourceId.valueOf("_class");

    /**
     * Instances of FormClass have one FormField: a label, which has its own
     * FormField id. It is defined at the application level to be a subproperty of
     * {@code _label}
     */
    public static final String LABEL_FIELD_ID = "_class_label";

    @NotNull
    private ResourceId id;
    private ResourceId ownerId;

    private String label;
    private String description;
    private final List<FormElement> elements = Lists.newArrayList();

    public FormClass(ResourceId id) {
        Preconditions.checkNotNull(id);
        this.id = id;
    }

    public ResourceId getOwnerId() {
        return ownerId;
    }

    public FormClass setOwnerId(ResourceId ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public ResourceId getParentId() {
        return ownerId;
    }

    public void setParentId(ResourceId resourceId) {
        setOwnerId(resourceId);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FormElement> getElements() {
        return elements;
    }

    public FormElement getElement(ResourceId elementId) {
        for (FormElement element : getAllElementsIncludingNested()) {
            if (element.getId().equals(elementId)) {
                return element;
            }
        }
        return null;
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

    @Override
    public String toString() {
        return "<FormClass: " + getLabel() + ">";
    }

    public static FormClass fromResource(Resource resource) {
        FormClass formClass = new FormClass(resource.getId());
        formClass.setOwnerId(resource.getOwnerId());
        formClass.setLabel(Strings.nullToEmpty(resource.isString(LABEL_FIELD_ID)));
        formClass.elements.addAll(fromRecords(resource.getRecordList("elements")));
        return formClass;
    }

    private static List<FormElement> fromRecords(List<Record> elementArray) {
        List<FormElement> elements = Lists.newArrayList();
        for (Record elementRecord : elementArray) {
            if ("section".equals(elementRecord.isString("type"))) {
                FormSection section = new FormSection(ResourceId.valueOf(elementRecord.getString("id")));
                section.setLabel(elementRecord.getString("label"));
                section.getElements().addAll(fromRecords(elementRecord.getRecordList("elements")));
                elements.add(section);
            } else {
                elements.add(FormField.fromRecord(elementRecord));
            }
        }
        return elements;
    }

    public Resource asResource() {
        Resource resource = Resources.createResource();
        resource.setId(id);
        resource.setOwnerId(ownerId);
        resource.set("classId", CLASS_ID);
        resource.set(LABEL_FIELD_ID, label);
        resource.set("elements", FormElement.asRecordList(elements));
        return resource;
    }


    /**
     * Normalized FormFields order. Puts built-in formfields at the end of the list.
     * Not super smart, one day (when we don't need built-in formfields encoded via id) we have to remove this method.
     * (todo: ask Alex whether it's ok or it's better to put it at the beginning of the list)
     */
    public void reorderFormFields() {
        Collections.sort(elements, new Comparator<FormElement>() {
            @Override
            public int compare(FormElement o1, FormElement o2) {
                boolean isBuitIn1 = o1.getId().asString().startsWith(getId().asString());
                boolean isBuitIn2 = o2.getId().asString().startsWith(getId().asString());
                if (isBuitIn1 && !isBuitIn2) {
                    return -1;
                } else if (!isBuitIn1 && isBuitIn2) {
                    return 1;
                }
                return 0;
            }
        });

    }

}
