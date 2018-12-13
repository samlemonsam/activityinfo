package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.AdminLevel;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.server.database.hibernate.entity.LocationType;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

public class GeoDatabaseProvider {

    public static final ResourceId GEODB_ID = ResourceId.valueOf("geodb");
    public static final String COUNTRY_ID_PREFIX = "country:";

    private final Provider<EntityManager> entityManager;

    @Inject
    public GeoDatabaseProvider(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    public boolean accept(ResourceId resourceId) {
        return resourceId.equals(GEODB_ID)
                || resourceId.getDomain() == CuidAdapter.COUNTRY_DOMAIN
                || resourceId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN
                || resourceId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN;
    }

    public boolean accept(String resource) {
        if (resource.startsWith(COUNTRY_ID_PREFIX)) {
            return true;
        }
        return accept(ResourceId.valueOf(resource));
    }

    public Optional<UserDatabaseMeta> queryGeoDb(int userId) {
        return Optional.of(new UserDatabaseMeta.Builder()
                .setDatabaseId(GEODB_ID)
                .setUserId(userId)
                .setLabel("Geographic Database")
                .setOwner(false)
                .setVersion("1")
                .setPublished(true)
                .build());
    }

    public List<CatalogEntry> findCatalogEntries(String parentId) {
        if(parentId.equals(GEODB_ID.asString())) {
            return queryCountries();
        } else if(parentId.startsWith(COUNTRY_ID_PREFIX)) {
            return queryCountryForms(parentId.substring(COUNTRY_ID_PREFIX.length()));
        }
        return Collections.emptyList();
    }

    private List<CatalogEntry> queryCountries() {
        return entityManager.get().createQuery("SELECT c " +
                "FROM Country c " +
                "ORDER BY c.name", Country.class)
                .getResultList().stream()
                .map(GeoDatabaseProvider::countryEntry)
                .collect(Collectors.toList());
    }

    private static CatalogEntry countryEntry(Country c) {
        return new CatalogEntry(countryId(c.getCodeISO()), c.getName(), CatalogEntryType.FOLDER);
    }

    private List<CatalogEntry> queryCountryForms(String countryId) {
        List<CatalogEntry> countryForms = new ArrayList<>();
        countryForms.addAll(queryAdminLevels(countryId));
        countryForms.addAll(queryLocationTypes(countryId));
        return countryForms;
    }

    private Collection<CatalogEntry> queryAdminLevels(String countryId) {
        return entityManager.get().createQuery("SELECT al " +
                "FROM AdminLevel al " +
                "WHERE al.country.codeISO = :code", AdminLevel.class)
                .setParameter("code", countryId)
                .getResultList().stream()
                .map(GeoDatabaseProvider::adminLevelEntry)
                .collect(Collectors.toList());
    }

    private Collection<CatalogEntry> queryLocationTypes(String countryId) {
        return entityManager.get().createQuery("SELECT lt " +
                "FROM LocationType lt " +
                "WHERE lt.boundAdminLevel IS NULL " +
                "AND lt.database IS NULL " +
                "AND lt.country.codeISO = :code", LocationType.class)
                .setParameter("code", countryId)
                .getResultList().stream()
                .map(GeoDatabaseProvider::locationFormEntry)
                .collect(Collectors.toList());
    }

    private static CatalogEntry adminLevelEntry(AdminLevel al) {
        return new CatalogEntry(CuidAdapter.adminLevelFormClass(al.getId()).asString(),
                al.getName(),
                CatalogEntryType.FORM);
    }

    private static CatalogEntry locationFormEntry(LocationType lt) {
        return new CatalogEntry(CuidAdapter.locationFormClass(lt.getId()).asString(),
                lt.getName(),
                CatalogEntryType.FORM);
    }

    private static String countryId(String isoCode2) {
        assert isoCode2.length() == 2;
        return COUNTRY_ID_PREFIX + isoCode2;
    }

}
