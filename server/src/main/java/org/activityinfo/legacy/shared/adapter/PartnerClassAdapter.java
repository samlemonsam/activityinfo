package org.activityinfo.legacy.shared.adapter;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;


public class PartnerClassAdapter {

    public static ResourceId getNameField(ResourceId classId) {
        return CuidAdapter.field(classId, CuidAdapter.NAME_FIELD);
    }

    public static ResourceId getFullNameField(ResourceId classId) {
        return CuidAdapter.field(classId, CuidAdapter.FULL_NAME_FIELD);
    }


    /**
     * Partner was a builtin object type in api1. However, we need a different
     * FormClass for each legacy UserDatabase.
     */
    public static FormClass create(int databaseId) {

        ResourceId classId = CuidAdapter.partnerFormClass(databaseId);
        FormClass formClass = new FormClass(classId);
        formClass.setLabel(I18N.CONSTANTS.partner());

        // add the partner's name
        FormField nameField = new FormField(getNameField(classId));
        nameField.setLabel(I18N.CONSTANTS.name());
        nameField.setType(TextType.INSTANCE);
        nameField.setRequired(true);
        formClass.addElement(nameField);

        // partner full name
        FormField fullNameField = new FormField(getFullNameField(classId));
        fullNameField.setLabel(I18N.CONSTANTS.fullName());
        fullNameField.setType(TextType.INSTANCE);
        formClass.addElement(fullNameField);

        return formClass;
    }


}
