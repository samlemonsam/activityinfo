package org.activityinfo.store.mysql.collections;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.GeodbFolder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.SimpleTable;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;

import java.sql.SQLException;


public class CountryTable implements SimpleTable {

    public static final String TABLE_NAME = "country";

    public static final ResourceId FORM_CLASS_ID = ResourceId.valueOf("country");
    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("name");
    public static final ResourceId CODE_FIELD_ID = ResourceId.valueOf("code");
    public static final ResourceId BOUNDARY_FIELD_ID = ResourceId.valueOf("boundary");


    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.equals(FORM_CLASS_ID);
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formId) throws SQLException {
        FormField nameField = new FormField(NAME_FIELD_ID);
        nameField.setCode("label");
        nameField.setLabel(I18N.CONSTANTS.name());
        nameField.setType(TextType.SIMPLE);

        FormField isoField = new FormField(CODE_FIELD_ID);
        isoField.setCode("code");
        isoField.setLabel(I18N.CONSTANTS.codeFieldLabel());
        isoField.setType(TextType.SIMPLE);
    
        FormField boundaryField = new FormField(BOUNDARY_FIELD_ID);
        boundaryField.setCode("boundary");
        boundaryField.setLabel(I18N.CONSTANTS.boundaries());
        boundaryField.setType(GeoAreaType.INSTANCE);

        // TODO: polygons

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(FORM_CLASS_ID, TABLE_NAME);
        mapping.setFormLabel("Country");
        mapping.setPrimaryKeyMapping(CuidAdapter.COUNTRY_DOMAIN, "countryId");
        mapping.setDatabaseId(GeodbFolder.GEODB_ID);
        mapping.addTextField(nameField, "name");
        mapping.addTextField(isoField, "iso2");
        mapping.addGeoAreaField(boundaryField);

        return mapping.build();
    }

}
