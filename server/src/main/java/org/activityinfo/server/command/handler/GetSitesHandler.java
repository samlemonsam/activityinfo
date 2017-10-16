package org.activityinfo.server.command.handler;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.collect.*;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
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
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
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

        /*if(command.isLegacyFetch() || !command.hasSingleActivity()) {
            return dispatcher.execute(new OldGetSites(command));
        }*/

        catalog = catalogProvider.get();
        builder = new ColumnSetBuilder(catalog, new AppEngineFormScanCache(), new FormSupervisorAdapter(catalog, user.getId()));
        formTreeBuilder = new FormTreeBuilder(catalog);

        List<ResourceId> activities = determineFetchActivities(command.filter());

        for (ResourceId activityId : activities) {
            FormTree formTree = formTreeBuilder.queryTree(activityId);
            FormClass activityForm = formTree.getFormClass(activityId);

            QueryModel query = buildQuery(activityForm, command);
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
                Map<Integer, Activity> activityMap = catalog.getActivityLoader()
                        .loadForDatabaseIds(filter.getRestrictions(DimensionType.Database));

                for (Activity activity : activityMap.values()) {
                    activityList.add(CuidAdapter.activityFormClass(activity.getId()));
                }
            } catch (SQLException excp) {
            }
        } else if (filter.getRestrictions(DimensionType.Activity).size() > 0) {
            for (Integer activity : filter.getRestrictions(DimensionType.Activity)) {
                activityList.add(CuidAdapter.activityFormClass(activity));
            }
        }

        return activityList;
    }

    private ExprNode determineFetchFilter(Filter commandFilter, FormTree formTree) {
        QueryFilter queryFilter = new QueryFilter(commandFilter, HashMultimap.<String, String>create());
        return queryFilter.composeFilter(formTree);
    }

    private QueryModel buildQuery(FormClass activityForm, GetSites command) {
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
            //buildLocationQuery(activityForm, ""); // Maybe add some extra handling so we dont fetch superfluous fields
            query.selectExpr("location").as("locationId");
            query.selectExpr("location.name").as("locationName");
            query.selectExpr("location.code").as("locationCode");
            query.selectExpr("location.point.latitude").as("locationLatitude");
            query.selectExpr("location.point.longitude").as("locationLongitude");
            query.selectExpr("location.admin").as("locationAdminId");
            query.selectExpr("location.admin.name").as("locationAdminName");
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

    private boolean hasField(FormClass form, ResourceId fieldId) {
        for (FormField field : form.getFields()) {
            if (field.getId().equals(fieldId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a field's form reference
     * Will always return the first reference in ReferenceType range
     * @param refField
     * @return ResourceId of reference form
     */
    private ResourceId getReferenceId(FormField refField) {
        assert (refField.getType() instanceof ReferenceType);
        Iterator<ResourceId> it = ((ReferenceType) refField.getType()).getRange().iterator();
        return it.next();
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
                case "locationAdminId":
                    // Can be null
                    String admin = columnViewMap.get("locationAdminId").getString(row);
                    if (admin != null) {
                        ResourceId adminEntity = ResourceId.valueOf(admin);
                        String adminName = columnViewMap.get("locationAdminName").getString(row);
                        //Integer level = determineAdminLevel(adminEntity);
                        site.setAdminEntity(0, new AdminEntityDTO(0, CuidAdapter.getLegacyIdFromCuid(adminEntity), adminName));
                    }
                case "locationAdminName":
                    // Handled above
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
                    break;
            }
        }
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

}
