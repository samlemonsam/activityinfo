package org.activityinfo.server.command.handler;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.OldGetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.query.impl.AppEngineFormScanCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.FormSupervisorAdapter;

import javax.inject.Provider;
import java.util.*;

public class GetSitesHandler implements CommandHandler<GetSites> {

    @Inject
    private Provider<MySqlCatalog> catalogProvider;

    @Inject
    private DispatcherSync dispatcher;

    private MySqlCatalog catalog;
    private ColumnSetBuilder builder;
    private QueryModel query;
    private ColumnSet resultColumns;

    private FormTreeBuilder formTreeBuilder;
    private FormTree formTree;
    private FormClass activityForm;

    @Override
    public SiteResult execute(GetSites command, User user) {

        if(command.isLegacyFetch() || !command.hasSingleActivity()) {
            return dispatcher.execute(new OldGetSites(command));
        }

        catalog = catalogProvider.get();
        builder = new ColumnSetBuilder(catalog, new AppEngineFormScanCache(), new FormSupervisorAdapter(catalog, user.getId()));
        formTreeBuilder = new FormTreeBuilder(catalog);

        ResourceId activityId = getActivityId(command);
        formTree = formTreeBuilder.queryTree(activityId);
        activityForm = formTree.getFormClass(activityId);

        buildQuery(command);
        resultColumns = builder.build(query);

        List<SiteDTO> sites = convertColumnsToSites();

        return new SiteResult(sites);
    }

    private void buildQuery(GetSites command) {
        query = new QueryModel(activityForm.getId());

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
            //buildLocationQuery(locationField); // Maybe add some extra handling so we dont fetch superfluous fields
            query.selectExpr("location").as("locationId");
            query.selectExpr("location.name").as("locationName");
            query.selectExpr("location.code").as("locationCode");
            query.selectExpr("location.point.latitude").as("locationLatitude");
            query.selectExpr("location.point.longitude").as("locationLongitude");
        }

        if (command.isFetchAttributes()) {
            buildAttributeQuery(activityForm);
        }

        if (command.fetchAnyIndicators()) {
            buildIndicatorQuery(command, activityForm);
        }

        if (command.isFetchComments()) {
            query.selectField("comments");
        }

    }

    private ResourceId getActivityId(GetSites command) {
        Iterator<Integer> activities = command.filter().getRestrictions(DimensionType.Activity).iterator();
        return CuidAdapter.activityFormClass(activities.next());
    }

    private void buildLocationQuery(FormField locationField) {
    }

    private void buildAttributeQuery(FormClass activityForm) {
        for (FormField field : activityForm.getFields()) {
            if (isAttribute(field.getId())) {
                query.selectField(field.getId());
            }
        }
    }

    private void buildIndicatorQuery(GetSites command, FormClass activityForm) {
        if (command.isFetchAllIndicators()) {
            for (FormField field : activityForm.getFields()) {
                if (isIndicator(field.getId())) {
                    query.selectField(field.getId());
                }
            }
        } else {
            for (Integer indicator : command.getFetchIndicators()) {
                query.selectField(CuidAdapter.indicatorField(indicator));
            }
        }
    }

    private boolean isAttribute(ResourceId id) {
        Character domain = id.getDomain();
        return domain.equals(CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN);
    }

    private boolean isIndicator(ResourceId id) {
        Character domain = id.getDomain();
        return domain.equals(CuidAdapter.INDICATOR_DOMAIN);
    }

    private List<SiteDTO> convertColumnsToSites() {
        int numRows = resultColumns.getNumRows();
        Map<String,ColumnView> columnViewMap = resultColumns.getColumns();

        List<SiteDTO> sites = initializeSiteList(numRows);

        for (int i=0; i < numRows; i++) {
            SiteDTO currSite = sites.get(i);
            populateSite(currSite, columnViewMap, i);
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

    private SiteDTO populateSite(SiteDTO site, Map<String,ColumnView> columnViewMap, int row) {
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
                    ResourceId location = ResourceId.valueOf(columnViewMap.get("locationId").getString(row));
                    site.setLocationId(CuidAdapter.getLegacyIdFromCuid(location));
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
                    if (isIndicator(colId)) {
                        site.setIndicatorValue(CuidAdapter.getLegacyIdFromCuid(colId), columnViewMap.get(column.getId()).get(row));
                        break;
                    }
                    if (isAttribute(colId)) {
                        addAttributeValues(site, columnViewMap, colId, row);
                        break;
                    }
                    break;
            }
        }
        return site;
    }

    private void addAttributeValues(SiteDTO site, Map<String,ColumnView> columnViewMap, ResourceId column, int row) {
        // TODO: Cannot add multi-select attributes due to current limitation of ColumnView model
        EnumColumnView attrColumnView = (EnumColumnView) columnViewMap.get(column.asString());
        FormField attrField = formTree.getRootField(column).getField();

        if (attrField.getType() instanceof EnumType) {
            EnumType enumType = (EnumType) attrField.getType();

            for (EnumItem item : enumType.getValues()) {
                ResourceId selectedId = ResourceId.valueOf(attrColumnView.getId(row));
                Boolean selected = item.getId().equals(selectedId);
                site.setAttributeValue(CuidAdapter.getLegacyIdFromCuid(item.getId()), selected);
                if (selected) {
                    site.addDisplayAttribute(enumType.toString(), item.getId().asString());
                }
            }
        }
    }

}
