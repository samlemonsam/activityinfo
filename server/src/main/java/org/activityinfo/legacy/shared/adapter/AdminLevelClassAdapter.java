package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;

import static org.activityinfo.model.legacy.CuidAdapter.adminLevelFormClass;

/**
 * Extracts a given AdminLevel from a provided SchemaDTO and converts it to a FormClass
 */
public class AdminLevelClassAdapter implements Function<SchemaDTO, FormClass> {

    private final int adminLevelId;

    public AdminLevelClassAdapter(int adminLevelId) {
        this.adminLevelId = adminLevelId;
    }


    public static ResourceId getNameFieldId(ResourceId classId) {
        return CuidAdapter.field(classId, CuidAdapter.NAME_FIELD);
    }

    @Override
    public FormClass apply(SchemaDTO schema) {
        AdminLevelDTO adminLevel = schema.getAdminLevelById(adminLevelId);

        ResourceId classId = adminLevelFormClass(adminLevelId);
        FormClass formClass = new FormClass(classId);
        formClass.setLabel(adminLevel.getName());

        if (adminLevel.isRoot()) {
            // TODO add country field
        } else {
            AdminLevelDTO parentLevel = schema.getAdminLevelById(adminLevel.getParentLevelId());
            FormField parentField = new FormField(CuidAdapter.field(classId, CuidAdapter.ADMIN_PARENT_FIELD))
            .setLabel(parentLevel.getName())
            .setSuperProperty(ApplicationProperties.PARENT_PROPERTY)
            .setType(ReferenceType.single(adminLevelFormClass(adminLevel.getParentLevelId())))
            .setRequired(true);
            formClass.addElement(parentField);
        }

        FormField nameField = new FormField(getNameFieldId(classId));
        nameField.setLabel(I18N.CONSTANTS.name());
        nameField.setType(TextType.INSTANCE);
        nameField.setSuperProperty(ApplicationProperties.LABEL_PROPERTY);
        nameField.setRequired(true);
        formClass.addElement(nameField);


        //    Not currently exposed by the legacy api
        //        FormField codeField = new FormField(CuidAdapter.field(classId, CuidAdapter.CODE_FIELD));
        //        codeField.setLabel(new LocalizedString(I18N.CONSTANTS.codeFieldLabel()));
        //        codeField.setType(FormFieldType.FREE_TEXT);
        //        formClass.addElement(codeField);

        return formClass;
    }
}
