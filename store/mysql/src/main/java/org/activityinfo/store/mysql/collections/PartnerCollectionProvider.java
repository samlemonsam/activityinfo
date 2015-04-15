package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;

import java.sql.SQLException;

import static org.activityinfo.model.legacy.CuidAdapter.NAME_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.field;


public class PartnerCollectionProvider implements MappingProvider {


    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.PARTNER_FORM_CLASS_DOMAIN;
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId classId) throws SQLException {
        int databaseId = CuidAdapter.getLegacyIdFromCuid(classId);
        
        TableMappingBuilder mapping = TableMappingBuilder.newMapping(classId, "partner");
        mapping.setFormLabel("Partner");
        mapping.setOwnerId(CuidAdapter.databaseId(databaseId));

        mapping.setPrimaryKeyMapping(CuidAdapter.PARTNER_DOMAIN, "partnerId");

        FormField nameField = new FormField(field(classId, NAME_FIELD))
                .setRequired(true)
                .setLabel("Name")
                .setCode("label")
          //      .setSuperProperty(ApplicationProperties.LABEL_PROPERTY)
                .setType(TextType.INSTANCE);
        
        mapping.addTextField(nameField, "name");
        
//        
//        FormField fullNameField = new FormField(field(classId, FULL_NAME_FIELD))
//                .setLabel("Full Name")
//            //    .setSuperProperty(ApplicationProperties.DESCRIPTION_PROPERTY)
//                .setRequired(false)
//                .setType(TextType.INSTANCE);

        return mapping.build();
        
    }
}
