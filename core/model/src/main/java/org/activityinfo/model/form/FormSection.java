package org.activityinfo.model.form;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;
import java.util.List;

import static org.activityinfo.json.Json.createObject;

/**
 * A logical group of {@code FormElements}
 *
 */
public class FormSection extends FormElement implements FormElementContainer {

    private final ResourceId id;
    private String label;
    private final List<FormElement> elements = Lists.newArrayList();

    public FormSection(ResourceId id) {
        Preconditions.checkNotNull(id);
        this.id = id;
    }

    public ResourceId getId() {
        return id;
    }

    @Override
    @Nonnull
    public String getLabel() {
        return label;
    }

    public FormSection setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public List<FormElement> getElements() {
        return elements;
    }

    @Override
    public FormSection addElement(FormElement element) {
        elements.add(element);
        return this;
    }

    public FormSection insertElement(int index, FormElement element) {
        elements.add(index, element);
        return this;
    }

    @Override
    public JsonValue toJsonObject() {
        JsonValue object = createObject();
        object.put("id", id.asString());
        object.put("label", label);
        object.put("type", "section");
        object.put("elements", FormClass.toJsonArray(elements));
        return object;
    }

    public static FormSection fromJson(JsonValue jsonObject) {
        FormSection section = new FormSection(ResourceId.valueOf(jsonObject.get("id").asString()));
        section.setLabel(jsonObject.get("label").asString());
        if(jsonObject.hasKey("elements")) {
            section.getElements().addAll(FormClass.fromJsonArray(jsonObject.get("elements")));
        }
        return section;
    }
    
    @Override
    public String toString() {
        return "FormSection{" +
                "id=" + id +
                ", label=" + label +
                '}';
    }

}
