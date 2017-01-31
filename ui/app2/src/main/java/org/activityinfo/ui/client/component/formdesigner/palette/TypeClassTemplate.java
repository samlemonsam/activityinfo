package org.activityinfo.ui.client.component.formdesigner.palette;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldTypeClass;

/**
 * FieldTemplate which creates a new FormField using the
 * default instance of a given TypeClass
 */
public class TypeClassTemplate implements FieldTemplate {

    private FieldTypeClass typeClass;
    private String label;

    public TypeClassTemplate(FieldTypeClass typeClass, String label) {
        this.typeClass = typeClass;
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public FormField create() {
        FormField formField = new FormField(ResourceId.generateFieldId(typeClass));
        formField.setType(typeClass.createType());
        formField.setLabel(label);
        return formField;
    }
}
