package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class UserMapping implements MappingProvider {

    private static final String TABLE_NAME = "userlogin";

    public static final ResourceId FORM_CLASS_ID = ResourceId.valueOf("_users");

    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("_users_name");
    public static final ResourceId EMAIL_FIELD_ID = ResourceId.valueOf("_email_field_id");

    private final TableMapping mapping;


    public UserMapping() {

        FormField nameField = new FormField(NAME_FIELD_ID);
        nameField.setLabel("Name");
        nameField.setType(TextType.INSTANCE);

        FormClass formClass = new FormClass(FORM_CLASS_ID);
        formClass.setOwnerId(ResourceId.ROOT_ID);
        formClass.addElement(nameField);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(TABLE_NAME);
        mapping.setFormClass(formClass);
        mapping.setPrimaryKeyMapping(CuidAdapter.USER_DOMAIN, "UserId");
        mapping.addTextField(nameField, "name");
        this.mapping = mapping.build();
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.equals(FORM_CLASS_ID);
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formClassId) {
        return mapping;
    }
}
