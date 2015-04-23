package org.activityinfo.core.shared.application;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;

/**
 * Defines a system-level FormClass of folders
 */
public class FolderClass {

    public static final ResourceId CLASS_ID = ResourceId.valueOf("_folder");

    public static final ResourceId LABEL_FIELD_ID = ResourceId.valueOf("_folder_label");

    public static final ResourceId DESCRIPTION_FIELD_ID = ResourceId.valueOf("_folder_description");

    public static final FormClass get() {

        FormField labelField = new FormField(LABEL_FIELD_ID);
        labelField.setSuperProperty(ApplicationProperties.LABEL_PROPERTY);

        FormField descriptionField = new FormField(DESCRIPTION_FIELD_ID);
        descriptionField.setSuperProperty(ApplicationProperties.DESCRIPTION_PROPERTY);

        FormClass formClass = new FormClass(CLASS_ID);
        formClass.addElement(labelField);
        formClass.addElement(descriptionField);

        return formClass;
    }
}