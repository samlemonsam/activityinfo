package org.activityinfo.store.mysql.collections;

import com.google.common.base.Preconditions;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;


public class DatabaseCollection implements MappingProvider {

    public static final String TABLE_NAME = "userdatabase";

    public static final ResourceId FORM_CLASS_ID = ResourceId.valueOf("FF8081814AE2C808014AE2CE1B6E0004");
    public static final ResourceId LABEL_FIELD_ID = ResourceId.valueOf("FF8081814AE2C808014AE2CE6D480005");
    public static final ResourceId DESCRIPTION_FIELD_ID = ResourceId.valueOf("FF8081814AE2C808014AE2CFEBFF0006");
    public static final ResourceId OWNER_FIELD_ID = ResourceId.valueOf("FF8081814AE2C808014AE2D01A310007");
    public static final ResourceId COUNTRY_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE459E82D0002");


    private final TableMapping mapping;

    public DatabaseCollection() {
        FormField labelField = new FormField(LABEL_FIELD_ID);
        labelField.setLabel(I18N.CONSTANTS.label());
        labelField.setCode("label");
        labelField.setRequired(true);
        labelField.setType(TextType.INSTANCE);

        FormField descriptionField = new FormField(DESCRIPTION_FIELD_ID);
        descriptionField.setCode("description");
        descriptionField.setLabel(I18N.CONSTANTS.description());
        descriptionField.setType(TextType.INSTANCE);

        FormField ownerField = new FormField(OWNER_FIELD_ID);
        ownerField.setCode("owner");
        ownerField.setLabel(I18N.CONSTANTS.ownerName());
        ownerField.setType(ReferenceType.single(UserCollection.FORM_CLASS_ID));
        ownerField.setRequired(true);

        FormField countryField = new FormField(COUNTRY_FIELD_ID);
        countryField.setCode("country");
        countryField.setLabel(I18N.CONSTANTS.country());
        countryField.setType(ReferenceType.single(CountryCollection.FORM_CLASS_ID));
        countryField.setRequired(true);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(FORM_CLASS_ID, TABLE_NAME);
        mapping.setPrimaryKeyMapping(CuidAdapter.DATABASE_DOMAIN, "databaseId");
        mapping.setOwnerId(ResourceId.ROOT_ID);
        mapping.addTextField(labelField, "name");
        mapping.addTextField(descriptionField, "fullName");
        mapping.addReferenceField(ownerField, CuidAdapter.USER_DOMAIN, "ownerUserId");
        mapping.addReferenceField(countryField, CuidAdapter.COUNTRY_DOMAIN, "countryId");
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
