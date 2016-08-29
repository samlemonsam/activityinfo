package org.activityinfo.store.mysql;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Geographic Database Folder
 */
public class GeodbFolder {

    public static final ResourceId GEODB_ID = ResourceId.valueOf("geodb");
    public static final String COUNTRY_ID_PREFIX = "country:";

    private QueryExecutor executor;

    public GeodbFolder(QueryExecutor executor) {
        this.executor = executor;
    }

    public CatalogEntry getRootEntry() {
        return new CatalogEntry(GEODB_ID.asString(), I18N.CONSTANTS.geography(), CatalogEntryType.FOLDER);
    }

    public List<CatalogEntry> getChildren(String parentId) throws SQLException {
        if(parentId.equals(GEODB_ID)) {
            return queryCountries();
        } else if(parentId.startsWith(COUNTRY_ID_PREFIX)) {
            return queryCountryForms(parentId.substring(COUNTRY_ID_PREFIX.length()));
        }
        return Collections.emptyList();
    }

    private List<CatalogEntry> queryCountries() throws SQLException {
        List<CatalogEntry> entries = new ArrayList<>();
        try(ResultSet rs = executor.query("SELECT iso2, name FROM country ORDER BY name")) {
            while(rs.next()) {
                entries.add(new CatalogEntry(
                        countryId(rs.getString(1)), 
                        rs.getString(2), CatalogEntryType.FOLDER));
            }
        }
        return entries;
    }
    
    private List<CatalogEntry> queryCountryForms(String countryId) throws SQLException {
        
        List<CatalogEntry> entries = new ArrayList<>();
        
        // Query Admin Levels
        try(ResultSet rs = executor.query("SELECT adminlevelid, name FROM adminlevel WHERE countryId IN " +
                " (SELECT countryId FROM country WHERE iso2 = ?) ", countryId)) {
            while(rs.next()) {
                String formId = CuidAdapter.adminLevelFormClass(rs.getInt(1)).asString();
                String label = rs.getString(2);
                entries.add(new CatalogEntry(formId, label, CatalogEntryType.FORM));
            }
        }
        // Query Location Types
        try(ResultSet rs = executor.query("SELECT locationTypeId, name FROM locationtype WHERE " +
                "boundAdminLevelId IS NULL AND " +
                "databaseId IS NULL AND " +
                "countryId IN (SELECT countryId FROM country WHERE iso2 = ?)  ", countryId)) {
            while(rs.next()) {
                String formId = CuidAdapter.locationFormClass(rs.getInt(1)).asString();
                String label = rs.getString(2);
                entries.add(new CatalogEntry(formId, label, CatalogEntryType.FORM));
            }
        }
        return entries;
    }
    
    private String countryId(String isoCode2) {
        assert isoCode2.length() == 2;
        return COUNTRY_ID_PREFIX + isoCode2;
    }
}
