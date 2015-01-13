package org.activityinfo.store.mysql.collections;

import com.google.common.base.Preconditions;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class UserDatabaseMapping implements MappingProvider {

    public static final String TABLE_NAME = "userdatabase";

    public static final ResourceId FORM_CLASS_ID = ResourceId.valueOf("_database");
    public static final ResourceId LABEL_FIELD_ID = ResourceId.valueOf("_database_label");
    public static final ResourceId DESCRIPTION_FIELD_ID = ResourceId.valueOf("_database_description");
    public static final ResourceId OWNER_FIELD_ID = ResourceId.valueOf("_database_owner");


    private final FormClass formClass;
    private final TableMapping mapping;

    public UserDatabaseMapping() {
        FormField labelField = new FormField(LABEL_FIELD_ID);
        labelField.setRequired(true);
        labelField.setType(TextType.INSTANCE);
        labelField.setLabel(I18N.CONSTANTS.label());

        FormField descriptionField = new FormField(DESCRIPTION_FIELD_ID);
        descriptionField.setType(TextType.INSTANCE);
        descriptionField.setLabel(I18N.CONSTANTS.description());

        FormField ownerField = new FormField(OWNER_FIELD_ID);
        ownerField.setType(ReferenceType.single(UserMapping.FORM_CLASS_ID));
        ownerField.setLabel(I18N.CONSTANTS.ownerName());


        formClass = new FormClass(FORM_CLASS_ID);
        formClass.setOwnerId(ResourceId.ROOT_ID);
        formClass.addElement(labelField);
        formClass.addElement(descriptionField);
        formClass.addElement(ownerField);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(TABLE_NAME);
        mapping.setPrimaryKeyMapping(CuidAdapter.DATABASE_DOMAIN, "databaseId");
        mapping.setFormClass(formClass);
        mapping.addTextField(labelField, "name");
        mapping.addTextField(descriptionField, "fullName");
        mapping.addReferenceField(ownerField, CuidAdapter.USER_DOMAIN, "ownerUserId");
        this.mapping = mapping.build();
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.equals(FORM_CLASS_ID);
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formClassId) {
        Preconditions.checkArgument(formClassId.equals(FORM_CLASS_ID));
        return mapping;
    }
}
