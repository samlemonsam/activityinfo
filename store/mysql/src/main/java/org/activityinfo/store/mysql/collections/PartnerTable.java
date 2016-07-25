package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.SimpleTable;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.metadata.DatabaseCache;

import java.sql.SQLException;

import static org.activityinfo.model.legacy.CuidAdapter.*;


public class PartnerTable implements SimpleTable {

    private final DatabaseCache databaseVersionMap;

    public PartnerTable(DatabaseCache databaseVersionMap) {
        this.databaseVersionMap = databaseVersionMap;
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.PARTNER_FORM_CLASS_DOMAIN;
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formId) throws SQLException {
        int databaseId = CuidAdapter.getLegacyIdFromCuid(formId);
        
        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formId, "partner");
        mapping.setFormLabel("Partner");
        mapping.setOwnerId(CuidAdapter.databaseId(databaseId));
        mapping.setPrimaryKeyMapping(CuidAdapter.PARTNER_DOMAIN, "partnerId");
        mapping.setFromClause("partnerindatabase pd LEFT JOIN partner base ON (pd.partnerId=base.partnerId)");
        mapping.setBaseFilter("pd.databaseId=" + databaseId);
        mapping.setVersion(databaseVersionMap.getSchemaVersion(databaseId));
        
        FormField nameField = new FormField(field(formId, NAME_FIELD))
                .setRequired(true)
                .setLabel("Name")
                .setCode("label")
          //      .setSuperProperty(ApplicationProperties.LABEL_PROPERTY)
                .setType(TextType.INSTANCE);
        
        mapping.addTextField(nameField, "name");
        
        
        FormField fullNameField = new FormField(field(formId, FULL_NAME_FIELD))
                .setLabel("Full Name")
            //    .setSuperProperty(ApplicationProperties.DESCRIPTION_PROPERTY)
                .setRequired(false)
                .setType(TextType.INSTANCE);
        
        mapping.addTextField(fullNameField, "FullName");

        return mapping.build();
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor queryExecutor, ResourceId id) throws SQLException {
        return Optional.absent();
    }
}
