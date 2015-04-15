package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class UserCollection implements MappingProvider {

    private static final String TABLE_NAME = "userlogin";

    public static final ResourceId FORM_CLASS_ID = ResourceId.valueOf("FF8081814AE2C808014AE2DEE2270009");
    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("FF8081814AE2C808014AE2DEFDAB000A");

    private final TableMapping mapping;

    public UserCollection() {

        FormField nameField = new FormField(NAME_FIELD_ID);
        nameField.setLabel("Name");
        nameField.setType(TextType.INSTANCE);
        nameField.setRequired(true);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(FORM_CLASS_ID, TABLE_NAME);
        mapping.setOwnerId(ResourceId.ROOT_ID);
        
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
