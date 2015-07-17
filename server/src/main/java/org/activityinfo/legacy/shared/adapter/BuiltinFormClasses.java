package org.activityinfo.legacy.shared.adapter;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;

/**
 * In the old legacy, many "FormClasses" were builtins, defined as part
 * of ActivityInfo's own datamodel. In Api2, these all move to the users
 * control as so become just another FormClass.
 */
public class BuiltinFormClasses {


    /**
     * Partner was a builtin object type in api1. However, we need a different
     * FormClass for each legacy UserDatabase.
     */
    public static FormClass projectFormClass(int databaseId) {

        ResourceId classId = CuidAdapter.projectFormClass(databaseId);
        FormClass formClass = new FormClass(classId);
        formClass.setLabel(I18N.CONSTANTS.project());

        // add the project's name
        FormField nameField = new FormField(CuidAdapter.field(classId, CuidAdapter.NAME_FIELD));
        nameField.setLabel(I18N.CONSTANTS.name());
        nameField.setType(TextType.INSTANCE);
        nameField.setRequired(true);
        formClass.addElement(nameField);

        return formClass;
    }

}
