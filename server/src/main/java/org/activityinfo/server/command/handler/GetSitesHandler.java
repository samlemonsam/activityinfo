package org.activityinfo.server.command.handler;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.collect.*;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.OldGetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.command.QueryFilter;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.query.impl.AppEngineFormScanCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.FormSupervisorAdapter;

import javax.inject.Provider;
import java.sql.SQLException;
import java.util.*;

import static java.lang.Double.NaN;

public class GetSitesHandler implements CommandHandler<GetSites> {

    @Inject
    private Provider<MySqlCatalog> catalogProvider;

    @Inject
    private DispatcherSync dispatcher;

    private MySqlCatalog catalog;
    private ColumnSetBuilder builder;

    private Map<ResourceId, QueryModel> queryMap = Maps.newHashMap();
    private Map<ResourceId, ColumnSet> resultColumnMap = Maps.newHashMap();

    private FormTreeBuilder formTreeBuilder;

    @Override
    public SiteResult execute(GetSites command, User user) {

        if (command.isLegacyFetch()) {
            return dispatcher.execute(new OldGetSites(command));
        }

        catalog = catalogProvider.get();
        builder = new ColumnSetBuilder(catalog, new AppEngineFormScanCache(), new FormSupervisorAdapter(catalog, user.getId()));
        formTreeBuilder = new FormTreeBuilder(catalog);

        List<ResourceId> activities = determineFetchActivities(command.filter());

        for (ResourceId activityId : activities) {
            FormTree formTree = formTreeBuilder.queryTree(activityId);
            FormClass activityForm = formTree.getFormClass(activityId);

            QueryModel query = buildQuery(formTree, activityForm, command);
            query.setFilter(determineFetchFilter(command.filter(), formTree));
            ColumnSet result = builder.build(query);

            queryMap.put(activityId, query);
            resultColumnMap.put(activityId, new ColumnSet(result.getNumRows(),result.getColumns()));
        }

        List<SiteDTO> sites = Lists.newArrayList();
        for (Map.Entry<ResourceId,ColumnSet> activityColumnSet : resultColumnMap.entrySet()) {
            sites.addAll(convertColumnsToSites(activityColumnSet.getKey(), activityColumnSet.getValue()));
        }

        return new SiteResult(sites);
    }

    private List<ResourceId> determineFetchActivities(Filter filter) {
        List<ResourceId> activityList = new ArrayList<>();

        if (filter.getRestrictions(DimensionType.Database).size() > 0) {
            try {
                Map<Integer, Activity> activityMap = catalog
                        .getActivityLoader()
                        .loadForDatabaseIds(filter.getRestrictions(DimensionType.Database));

                for (Activity activity : activityMap.values()) {
                    activityList.add(CuidAdapter.activityFormClass(activity.getId()));
                }
            } catch (SQLException excp) {
            }
        }
        if (filter.getRestrictions(DimensionType.Activity).size() > 0) {
            for (Integer activity : filter.getRestrictions(DimensionType.Activity)) {
                activityList.add(CuidAdapter.activityFormClass(activity));
            }
        }
        if (activityList.isEmpty()) {
            throw new CommandException("Request too broad: must filter by database or activity");
        }

        return activityList;
    }

    private ExprNode determineFetchFilter(Filter commandFilter, FormTree formTree) {
        QueryFilter queryFilter = new QueryFilter(commandFilter, HashMultimap.<String, String>create());
        return queryFilter.composeFilter(formTree);
    }

    private QueryModel buildQuery(FormTree formTree, FormClass activityForm, GetSites command) {
        QueryModel query = new QueryModel(activityForm.getId());

        query.selectResourceId().as("id");
        query.selectClassId().as("activity");

        query.selectField("project").as("projectId");
        query.selectExpr("project.name").as("projectName");

        if (command.isFetchDates()) {
            query.selectField("date1").as("startDate");
            query.selectField("date2").as("endDate");
        }

        if (command.isFetchPartner()) {
            query.selectExpr("partner").as("partnerId");
            query.selectExpr("partner.name").as("partnerName");
        }

        if (command.isFetchLocation()) {
            query = buildLocationQuery(query,formTree, activityForm);
        }

        if (command.isFetchAttributes()) {
            query = buildAttributeQuery(query, activityForm);
        }

        if (command.fetchAnyIndicators()) {
            query = buildIndicatorQuery(query, command, activityForm);
        }

        if (command.isFetchComments()) {
            query.selectField("comments");
        }

        return query;
    }

    private QueryModel buildLocationQuery(QueryModel query, FormTree formTree, FormClass form) {

        switch (form.getId().getDomain()) {
            case CuidAdapter.ACTIVITY_DOMAIN:
                FormField locationField = getField(form, CuidAdapter.field(form.getId(), CuidAdapter.LOCATION_FIELD));
                if (locationField != null) {
                    ResourceId locationReferenceId = getReferenceId(locationField.getType());
                    return buildLocationQuery(query, formTree, formTree.getFormClass(locationReferenceId));
                } else {
                    // country form, get country from db
                    return query;
                }

            case CuidAdapter.LOCATION_TYPE_DOMAIN:
                query.selectExpr(form.getId().asString() + "._id").as("locationId");
                query.selectExpr(form.getId().asString() + ".name").as("locationName");
                query.selectExpr(form.getId().asString() + ".code").as("locationCode");

                FormField geoField = getField(form, CuidAdapter.field(form.getId(), CuidAdapter.GEOMETRY_FIELD));
                if (geoField != null) {
                    query = buildGeoLocationQuery(query, formTree, geoField);
                }

                FormField adminField = getField(form, CuidAdapter.field(form.getId(), CuidAdapter.ADMIN_FIELD));
                if (adminField != null) {
                    ResourceId adminReferenceId = getReferenceId(adminField.getType());
                    return buildLocationQuery(query, formTree, formTree.getFormClass(adminReferenceId));
                }

                return query;

            case CuidAdapter.ADMIN_LEVEL_DOMAIN:
                query.selectExpr(form.getId().asString() + "._id").as(form.getId().asString());
                query.selectExpr(form.getId().asString() + ".name").as(form.getId().asString() + ".Name");
                query.selectExpr("\"" + form.getId().asString() + "\"").as(form.getId().asString() + ".LevelId");
                query.selectExpr("\"" + form.getLabel() + "\"").as(form.getId().asString() + ".Level");

                FormField parentLevel = getField(form, CuidAdapter.field(form.getId(), CuidAdapter.ADMIN_PARENT_FIELD));
                if (parentLevel != null) {
                    query.selectExpr(form.getId().asString() + ".parent").as(form.getId().asString() + ".Parent");
                    // TODO: More complex admin entity structure - parents of parents...
                    //ResourceId parentLevelId = getReferenceId(parentLevel.getType());
                    //return buildLocationQuery(query, formTree, formTree.getFormClass(parentLevelId));
                }

                return query;

            default:
                // undefined location form...
                return query;
        }
    }

    private ResourceId getReferenceId(FieldType type) {
        if (type instanceof ReferenceType)
            return getReferenceId((ReferenceType) type);
        else
            throw new IllegalArgumentException("Given FieldType " + type + " should be of reference type");
    }

    private ResourceId getReferenceId(ReferenceType referenceType) {
        if (!referenceType.getRange().isEmpty()) {
            Iterator<ResourceId> it = referenceType.getRange().iterator();
            return it.next();
        }
        throw new IllegalArgumentException("Given ReferenceType " + referenceType + " has no reference ids in range");
    }

    private FormField getField(FormClass form, ResourceId fieldId) {
        try {
            return form.getField(fieldId);
        } catch (IllegalArgumentException excp) {
            return null;
        }
    }

    private QueryModel buildGeoLocationQuery(QueryModel query, FormTree formTree, FormField geoField) {
        if (geoField.getType() instanceof GeoPointType) {
            query.selectExpr(geoField.getId() + ".latitude").as("locationLatitude");
            query.selectExpr(geoField.getId() + ".longitude").as("locationLongitude");
        }
        return query;
    }

    private QueryModel buildAttributeQuery(QueryModel query, FormClass activityForm) {
        for (FormField field : activityForm.getFields()) {
            if (field.getType() instanceof EnumType) {
                EnumType enumType = (EnumType) field.getType();
                buildEnumItemQuery(query, field.getId(), enumType);
            }
        }
        return query;
    }

    private QueryModel buildEnumItemQuery(QueryModel query, ResourceId fieldId, EnumType enumType) {
        for (EnumItem enumItem : enumType.getValues()) {
            ExprNode expr = new CompoundExpr(fieldId, enumItem.getId().asString());
            query.selectExpr(expr).as(enumItem.getId().asString());
        }
        return query;
    }

    private QueryModel buildIndicatorQuery(QueryModel query, GetSites command, FormClass activityForm) {
        if (command.isFetchAllIndicators()) {
            for (FormField field : activityForm.getFields()) {
                if (isDomain(field.getId(), CuidAdapter.INDICATOR_DOMAIN)) {
                    query.selectField(field.getId());
                }
            }
        } else {
            for (Integer indicator : command.getFetchIndicators()) {
                query.selectField(CuidAdapter.indicatorField(indicator));
            }
        }
        return query;
    }

    private boolean isDomain(ResourceId id, char domain) {
        Character idDomain = id.getDomain();
        return idDomain.equals(domain);
    }

    private List<SiteDTO> convertColumnsToSites(ResourceId activityId, ColumnSet columns) {
        int numRows = columns.getNumRows();
        Map<String,ColumnView> columnViewMap = columns.getColumns();

        List<SiteDTO> sites = initializeSiteList(numRows);

        for (int i=0; i < numRows; i++) {
            SiteDTO currSite = sites.get(i);
            populateSite(queryMap.get(activityId), currSite, columnViewMap, i);
        }

        return sites;
    }

    private List<SiteDTO> initializeSiteList(int numRows) {
        List<SiteDTO> siteList = new ArrayList<>(numRows);

        for (int i=0; i < numRows; i++) {
            siteList.add(new SiteDTO());
        }

        return siteList;
    }

    private SiteDTO populateSite(QueryModel query, SiteDTO site, Map<String,ColumnView> columnViewMap, int row) {
        Map<ResourceId,AdminEntityDTO> adminLevelMap = Maps.newHashMap();

        for (ColumnModel column : query.getColumns()) {
            switch (column.getId()) {
                case "id":
                    ResourceId id = ResourceId.valueOf(columnViewMap.get("id").getString(row));
                    site.setId(CuidAdapter.getLegacyIdFromCuid(id));
                    break;

                case "activity":
                    ResourceId activity = ResourceId.valueOf(columnViewMap.get("activity").getString(row));
                    site.setActivityId(CuidAdapter.getLegacyIdFromCuid(activity));
                    break;

                case "partnerId":
                    ResourceId partnerId = ResourceId.valueOf(columnViewMap.get("partnerId").getString(row));
                    String partnerName = columnViewMap.get("partnerName").getString(row);
                    site.setPartner(new PartnerDTO(CuidAdapter.getLegacyIdFromCuid(partnerId), partnerName));
                case "partnerName":
                    // Handled above
                    break;

                case "projectId":
                    // Can be null
                    String project = columnViewMap.get("projectId").getString(row);
                    if (project != null) {
                        ResourceId projectId = ResourceId.valueOf(project);
                        String projectName = columnViewMap.get("projectName").getString(row);
                        site.setProject(new ProjectDTO(CuidAdapter.getLegacyIdFromCuid(projectId), projectName));
                    }
                case "projectName":
                    // Handled above
                    break;

                case "startDate":
                    LocalDate startDate = LocalDate.parse(columnViewMap.get("startDate").getString(row));
                    site.setDate1(startDate);
                    break;
                case "endDate":
                    LocalDate endDate = LocalDate.parse(columnViewMap.get("endDate").getString(row));
                    site.setDate2(endDate);
                    break;

                case "locationId":
                    // Can be null - must generate a random location otherwise SiteDTO JSON conversion will fail
                    String location = columnViewMap.get("locationId").getString(row);
                    if (location != null) {
                        ResourceId locationId = ResourceId.valueOf(location);
                        site.setLocationId(CuidAdapter.getLegacyIdFromCuid(location));
                    } else {
                        ResourceId locationId = CuidAdapter.generateLocationCuid();
                        site.setLocationId(CuidAdapter.getLegacyIdFromCuid(locationId));
                    }
                    break;
                case "locationName":
                    site.setLocationName(columnViewMap.get("locationName").getString(row));
                    break;
                case "locationCode":
                    site.setLocationAxe(columnViewMap.get("locationCode").getString(row));
                    break;
                case "locationLatitude":
                    site.setY(columnViewMap.get("locationLatitude").getDouble(row));
                    break;
                case "locationLongitude":
                    site.setX(columnViewMap.get("locationLongitude").getDouble(row));
                    break;

                case "comments":
                    site.setComments(columnViewMap.get("comments").getString(row));
                    break;

                default:
                    // Attribute and Indicator fields
                    ResourceId colId = ResourceId.valueOf(column.getId());
                    if (isDomain(colId, CuidAdapter.INDICATOR_DOMAIN)) {
                        Object value = columnViewMap.get(column.getId()).get(row);
                        if (!value.equals(NaN)) {
                            site.setIndicatorValue(CuidAdapter.getLegacyIdFromCuid(colId), value);
                        }
                        break;
                    }
                    if (isDomain(colId, CuidAdapter.ATTRIBUTE_DOMAIN)) {
                        if (column.getExpression() instanceof CompoundExpr) {
                            CompoundExpr compound = (CompoundExpr) column.getExpression();
                            addAttributeValue(site, columnViewMap, compound, row);
                        }
                        break;
                    }
                    if (isDomain(colId, CuidAdapter.ADMIN_LEVEL_DOMAIN)) {
                        addAdminLevel(adminLevelMap, columnViewMap, colId, row);
                }
                    break;
            }
        }
        site = insertAdminLevels(site, adminLevelMap);
        return site;
    }

    private void addAttributeValue(SiteDTO site, Map<String,ColumnView> columnViewMap, CompoundExpr attrExpr, int row) {
        String attrGroupId = attrExpr.getValue().asExpression();
        String attrId = attrExpr.getField().asExpression();

        BitSetColumnView bitSetColumnView = (BitSetColumnView) columnViewMap.get(attrId);

        Boolean selected;
        switch (bitSetColumnView.getBoolean(row)) {
            case BitSetColumnView.TRUE:
                selected = true;
                break;
            case BitSetColumnView.FALSE:
                selected = false;
                break;
            default:
                selected = null;
                break;
        }

        site.setAttributeValue(CuidAdapter.getLegacyIdFromCuid(attrId), selected);
        if (selected) {
            site.addDisplayAttribute(attrGroupId, attrId);
        }
    }

    private void addAdminLevel(Map<ResourceId,AdminEntityDTO> adminLevelMap, Map<String,ColumnView> columnViewMap, ResourceId colId, int row) {
        if (colId.asString().contains(".Name")) {
            ResourceId adminId = getAdminEntityId(colId, ".Name");
            AdminEntityDTO adminEntity = getAdminEntityDTO(adminId, adminLevelMap);
            String adminEntityName = columnViewMap.get(colId.asString()).getString(row);
            adminEntity.setName(adminEntityName);
        } else if (colId.asString().contains(".LevelId")) {
            ResourceId adminId = getAdminEntityId(colId, ".LevelId");
            AdminEntityDTO adminEntity = getAdminEntityDTO(adminId, adminLevelMap);
            ResourceId adminLevelId = ResourceId.valueOf(columnViewMap.get(colId.asString()).getString(row));
            adminEntity.setLevelId(CuidAdapter.getLegacyIdFromCuid(adminLevelId));
        } else if (colId.asString().contains(".Level")) {
            ResourceId adminId = getAdminEntityId(colId,".Level");
            AdminEntityDTO adminEntity = getAdminEntityDTO(adminId, adminLevelMap);
            String adminLevelName = columnViewMap.get(colId.asString()).getString(row);
            adminEntity.setLevelName(adminLevelName);
        } else if (colId.asString().contains(".Parent")) {
            ResourceId adminId = getAdminEntityId(colId,".Parent");
            AdminEntityDTO adminEntity = getAdminEntityDTO(adminId, adminLevelMap);
            ResourceId adminParentId = ResourceId.valueOf(columnViewMap.get(colId.asString()).getString(row));
            adminEntity.setParentId(CuidAdapter.getLegacyIdFromCuid(adminParentId));
        } else {
            AdminEntityDTO adminEntity = getAdminEntityDTO(colId, adminLevelMap);
            ResourceId adminEntityId = ResourceId.valueOf(columnViewMap.get(colId.asString()).getString(row));
            adminEntity.setId(CuidAdapter.getLegacyIdFromCuid(adminEntityId));
        }
    }

    private SiteDTO insertAdminLevels(SiteDTO site, Map<ResourceId,AdminEntityDTO> adminLevelMap) {
        for(Map.Entry<ResourceId,AdminEntityDTO> entry : adminLevelMap.entrySet()) {
            AdminEntityDTO adminEntity = entry.getValue();
            site.setAdminEntity(adminEntity.getLevelId(), adminEntity);
        }
        return site;
    }

    private ResourceId getAdminEntityId(ResourceId colId, String toRemove) {
        return ResourceId.valueOf(colId.asString().replace(toRemove,""));
    }

    private AdminEntityDTO getAdminEntityDTO(ResourceId adminEntityId, Map<ResourceId,AdminEntityDTO> adminLevelMap) {
        if (!adminLevelMap.containsKey(adminEntityId)) {
            adminLevelMap.put(adminEntityId, new AdminEntityDTO());
        }
        return adminLevelMap.get(adminEntityId);
    }

}
