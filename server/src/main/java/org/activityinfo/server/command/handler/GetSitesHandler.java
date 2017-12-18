package org.activityinfo.server.command.handler;

import com.extjs.gxt.ui.client.data.SortInfo;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.impl.OldGetSitesHandler;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.command.QueryFilter;
import org.activityinfo.server.command.handler.binding.*;
import org.activityinfo.server.command.handler.binding.dim.*;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.mysql.MySqlCatalog;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.CountryInstance;
import org.activityinfo.store.query.server.*;
import org.activityinfo.store.query.shared.*;
import org.activityinfo.store.spi.BatchingFormTreeBuilder;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class GetSitesHandler implements CommandHandler<GetSites> {

    private static final Logger LOGGER = Logger.getLogger(GetSitesHandler.class.getName());

    @Inject
    private Provider<MySqlCatalog> catalogProvider;

    @Inject
    private DispatcherSync dispatcher;

    private GetSites command;

    private MySqlCatalog catalog;
    private ColumnSetBuilder builder;
    private BatchingFormTreeBuilder batchFormTreeBuilder;
    private FormScanBatch batch;
    private SortInfo sortInfo;

    private Map<ResourceId,FormTree> formTreeMap;
    private Map<ResourceId,QueryModel> queryMap = new HashMap<>();
    private Map<ResourceId,List<FieldBinding>> fieldBindingMap = new HashMap<>();
    private List<Runnable> queryResultHandlers = new ArrayList<>();
    private Map<ResourceId,List<ResourceId>> locationMap = new HashMap<>();

    private Map<Integer,Activity> activities;

    private int offset;
    private int limit;
    private int totalResultLength;

    private List<SiteDTO> siteList = Lists.newArrayList();
    private List<SiteDTO> monthlySiteList = Lists.newArrayList();
    private Map<ResourceId,SiteDTO> monthlyRootSiteMap = Maps.newHashMap();

    private final Stopwatch metadataTime = Stopwatch.createUnstarted();
    private final Stopwatch treeTime = Stopwatch.createUnstarted();
    private final Stopwatch queryBuildTime = Stopwatch.createUnstarted();
    private final Stopwatch queryExecTime = Stopwatch.createUnstarted();
    private final Stopwatch monthlyMergeTime = Stopwatch.createUnstarted();
    private final Stopwatch aggregateTime = Stopwatch.createUnstarted();

    @Override
    public SiteResult execute(GetSites cmd, User user) {

        if (useLegacyMethod(cmd, user)) {
            return dispatcher.execute(new OldGetSites(cmd));
        }
        LOGGER.info("Entering execute()");
        aggregateTime.start();
        try {
            initialiseHandler(cmd, user);
            fetchActivityMetadata(cmd.getFilter());
            queryFormTrees();
            buildQueries();
            batchQueries();
            executeBatch();
            mergeMonthlyRootSites();
        } catch (CommandException excp) {
            // If we catch a *Command* Exception, lets try the legacy method
            return dispatcher.execute(new OldGetSites(cmd));
        }
        aggregateTime.stop();

        printTimes();
        LOGGER.info("Exiting execute()");

        SiteResult result = new SiteResult(siteList);
        result.setOffset(cmd.getOffset());
        result.setTotalLength(totalResultLength);
        return result;
    }

    private boolean useLegacyMethod(GetSites command, User user) {
        return user == null
                || command.getFilter() == null
                || command.isLegacyFetch();
    }

    private void printTimes() {
        LOGGER.info("GetSites timings: {" + "Metadata Fetch: " + metadataTime.toString() + "; " +
                "Form Tree Fetch: " + treeTime.toString() + "; " +
                "Query Build: " + queryBuildTime.toString() + "; " +
                "Query Execution : " + queryExecTime.toString() + "; " +
                "Monthly Indicator Merge: " + monthlyMergeTime.toString() + "; " +
                "Aggregate Time: " + aggregateTime.toString()
        + "}");
    }

    private void initialiseHandler(GetSites command, User user) {
        catalog = catalogProvider.get();
        if (catalog != null) {
            this.command = command;
            builder = new ColumnSetBuilder(catalog, new AppEngineFormScanCache(), new FormSupervisorAdapter(catalog, user.getId()));
            batchFormTreeBuilder = new BatchingFormTreeBuilder(catalog);
            batch = builder.createNewBatch();
            sortInfo = command.getSortInfo();
            offset = command.getOffset();
            limit = command.getLimit();
            totalResultLength = 0;
        } else {
            throw new CommandException("Could not retrieve form catalog");
        }
    }

    private void fetchActivityMetadata(Filter filter) {
        try {
            metadataTime.start();
            activities = loadMetadata(filter);
        } catch (SQLException excp) {
            throw new CommandException("Could not fetch metadata from server");
        } finally {
            metadataTime.stop();
        }
    }

    private Map<Integer,Activity> loadMetadata(Filter filter) throws SQLException {
        if (filter.isRestricted(DimensionType.Database)) {
            return catalog.getActivityLoader().loadForDatabaseIds(filter.getRestrictions(DimensionType.Database));
        } else if (filter.isRestricted(DimensionType.Activity)) {
            return catalog.getActivityLoader().load(filter.getRestrictions(DimensionType.Activity));
        } else {
            throw new CommandException("Request too broad: must filter by Database or Activity");
        }
    }

    private void queryFormTrees() {
        treeTime.start();
        Set<ResourceId> formIds = new HashSet<>();
        ResourceId activityFormId;
        for (Activity activity : activities.values()) {
            if (reject(activity)) {
                continue;
            }
            activityFormId = activity.getSiteFormClassId();
            formIds.add(activityFormId);
            if (activity.isMonthly() && command.isFetchAllReportingPeriods()) {
                formIds.add(CuidAdapter.reportingPeriodFormClass(CuidAdapter.getLegacyIdFromCuid(activityFormId)));
            }
        }
        formTreeMap = batchFormTreeBuilder.queryTrees(formIds);
        treeTime.stop();
    }

    private boolean reject(Activity activity) {
        return activity.isDeleted() || !activity.isClassicView();
    }

    private void buildQueries() {
        queryBuildTime.start();
        for (Map.Entry<ResourceId, FormTree> formTreeEntry : formTreeMap.entrySet()) {
            QueryModel query = buildQuery(formTreeEntry.getValue());
            query.setFilter(determineQueryFilter(command.getFilter(), formTreeEntry.getValue()));
            setQuerySort(query, formTreeEntry.getValue());
            queryMap.put(formTreeEntry.getKey(), query);
        }
        queryBuildTime.stop();
    }

    private void setQuerySort(QueryModel query, FormTree tree) {
        if (sortInfo != null) {
            SortModel sortModel;
            switch(sortInfo.getSortDir()) {
                case ASC:
                    sortModel = new SortModel(parseSortColumn(sortInfo.getSortField()), SortModel.Dir.ASC);
                    query.addSortModel(sortModel);
                    break;
                case DESC:
                    sortModel = new SortModel(parseSortColumn(sortInfo.getSortField()), SortModel.Dir.DESC);
                    query.addSortModel(sortModel);
                    break;
            }
        }
    }

    private String parseSortColumn(String sortField) {
        if (sortField == null) {
            return null;
        }
        if (sortField.equals("date1")) {
            return StartEndDateFieldBinding.START_DATE_COLUMN;
        } else if (sortField.equals("date2")) {
            return StartEndDateFieldBinding.END_DATE_COLUMN;
        } else if (sortField.equals("locationName")) {
            return LocationFieldBinding.LOCATION_NAME_COLUMN;
        } else if (sortField.equals("partner")) {
            return PartnerDimBinding.PARTNER_LABEL_COLUMN;
        } else if (sortField.equals("project")) {
            return ProjectDimBinding.PROJECT_LABEL_COLUMN;
        } else if (sortField.equals("locationAxe")) {
            return LocationFieldBinding.LOCATION_CODE_COLUMN;
        } else if (sortField.startsWith(IndicatorDTO.PROPERTY_PREFIX)) {
            int indicatorId = IndicatorDTO.indicatorIdForPropertyName(sortField);
            return CuidAdapter.indicatorField(indicatorId).asString();
        } else if (OldGetSitesHandler.adminLevelId(sortField).isPresent()) {
            Integer intId = OldGetSitesHandler.adminLevelId(sortField).get();
            ResourceId adminLevelId = CuidAdapter.adminLevelFormClass(intId);
            return (new CompoundExpr(new SymbolExpr(adminLevelId), LocationFieldBinding.NAME_SYMBOL)).toString();
        } else {
            LOGGER.warning("Unimplemented sort on GetSites: '" + sortField + "");
            return null;
        }
    }

    private void batchQueries() {
        for (final Map.Entry<ResourceId,QueryModel> queryEntry : queryMap.entrySet()) {
            enqueueQuery(queryEntry.getValue(), new Function<ColumnSet, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ColumnSet columnSet) {
                    List<FieldBinding> fieldBindings = fieldBindingMap.get(queryEntry.getKey());

                    if (monthlyReportForm(queryEntry.getKey())) {
                        extractMonthlySites(fieldBindings, columnSet);
                    } else {
                        extractSites(fieldBindings, columnSet);
                    }

                    return null;
                }
            });
        }
    }

    private void extractMonthlySites(List<FieldBinding> fieldBindings, ColumnSet columnSet) {
        if (acceptResult(columnSet.getNumRows())) {
            SiteDTO[] extractedSiteArray = extractSiteData(fieldBindings,  columnSet);
            List<SiteDTO> extractedSiteList = Lists.newArrayList(extractedSiteArray);
            siteList.addAll(extractedSiteList);
            monthlySiteList.addAll(extractedSiteList);
        }
    }

    private void extractSites(List<FieldBinding> fieldBindings, ColumnSet columnSet) {
        if (acceptResult(columnSet.getNumRows())) {
            if (command.isFetchAllReportingPeriods()) {
                SiteDTO[] sites = extractSiteData(fieldBindings, columnSet);
                addMonthlyRootSites(sites);
            } else {
                totalResultLength = totalResultLength + columnSet.getNumRows();
                SiteDTO[] sites = extractSiteData(fieldBindings, columnSet);
                siteList.addAll(Lists.newArrayList(sites));
            }
        }
    }

    private boolean acceptResult(int numResults) {
        if ((limit > 0) && (siteList.size() >= limit)) {
            return false;
        }
        if ((offset > 0) && (numResults < offset)) {
            offset = offset - numResults;
            return false;
        }
        return true;
    }

    private void addMonthlyRootSites(SiteDTO[] sites) {
        for (SiteDTO site : sites) {
            monthlyRootSiteMap.put(site.getInstanceId(), site);
        }
    }

    private SiteDTO[] initialiseSites(int length) {
        SiteDTO[] array = new SiteDTO[length];
        for (int i=0; i<array.length; i++) {
            array[i] = new SiteDTO();
        }
        return array;
    }

    private SiteDTO[] extractSiteData(List<FieldBinding> fieldBindings, ColumnSet columnSet) {
        ColumnSet finalColumnSet;
        SiteDTO[] sites;

        if (offset > 0 || limit > 0) {
            Map<String,ColumnView> paginatedColumns = Maps.newHashMap();
            int[] index = generatePaginationIndex(columnSet.getNumRows());
            sites = initialiseSites(index.length);

            for (Map.Entry<String,ColumnView> column : columnSet.getColumns().entrySet()) {
                paginatedColumns.put(column.getKey(), column.getValue().select(index));
            }

            finalColumnSet = new ColumnSet(index.length, paginatedColumns);
        } else {
            sites = initialiseSites(columnSet.getNumRows());
            finalColumnSet = columnSet;
        }

        for (FieldBinding binding : fieldBindings) {
            binding.extractFieldData(sites, finalColumnSet);
        }

        return sites;
    }

    private int[] generatePaginationIndex(int numResultRows) {
        int pageOffset = (offset > 0) ? offset : 0;
        int pageLimit;

        if ((limit > 0) && (numResultRows > limit-siteList.size())) {
            pageLimit = limit - siteList.size();
        } else {
            pageLimit = numResultRows;
        }

        int[] pageIndex = new int[pageLimit-pageOffset];

        for (int i=0; i<pageIndex.length; i++) {
            pageIndex[i] = pageOffset + i;
        }

        offset = offset - pageOffset;
        return pageIndex;
    }

    private void enqueueQuery(QueryModel query, final Function<ColumnSet,Void> handler) {
        final Slot<ColumnSet> result = builder.enqueue(query, batch);
        queryResultHandlers.add(new Runnable() {
            @Override
            public void run() {
                ColumnSet columnSet = result.get();
                handler.apply(columnSet);
            }
        });
    }

    private void executeBatch() {
        try {
            queryExecTime.start();
            builder.execute(batch);
        } catch (Exception excp) {
            throw new RuntimeException("Failed to execute query batch", excp);
        }
        for (Runnable handler : queryResultHandlers) {
            handler.run();
        }
        queryExecTime.stop();
    }

    private void mergeMonthlyRootSites() {
        monthlyMergeTime.start();
        for (SiteDTO monthlySite : monthlySiteList) {
            if (monthlyRootSiteMap.containsKey(monthlySite.getInstanceId())) {
                SiteDTO rootSite = monthlyRootSiteMap.get(monthlySite.getInstanceId());
                monthlySite.setLocation(rootSite.getLocation());
                monthlySite.setProject(rootSite.getProject());
                monthlySite.setPartner(rootSite.getPartner());
            }
        }
        monthlyMergeTime.stop();
    }

    private QueryModel buildQuery(FormTree formTree) {
        Activity activity = activities.get(CuidAdapter.getLegacyIdFromCuid(formTree.getRootFormId()));
        if (monthlyReportForm(formTree.getRootFormId())) {
            return buildMonthlyQuery(activity, formTree, formTree.getRootFormClass());
        } else {
            return buildQuery(activity, formTree, formTree.getRootFormClass());
        }
    }

    private boolean monthlyReportForm(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS;
    }

    private ExprNode determineQueryFilter(Filter commandFilter, FormTree formTree) {
        QueryFilter queryFilter = new QueryFilter(commandFilter, HashMultimap.<String, String>create());
        return queryFilter.composeFilter(formTree);
    }

    private QueryModel buildMonthlyQuery(Activity activity, FormTree formTree, FormClass form) {
        QueryModel query = new QueryModel(form.getId());
        fieldBindingMap.put(form.getId(), Lists.<FieldBinding>newLinkedList());

        addBinding(new SiteDimBinding(), query, formTree);
        addBinding(new ActivityIdFieldBinding(), query, formTree);
        addBinding(new StartEndDateFieldBinding(), query, formTree);
        if (command.fetchAnyIndicators()) {
            query = buildIndicatorQuery(query, formTree, form);
        }
        if (command.isFetchAttributes()) {
            query = buildAttributeQuery(query, formTree, form);
        }
        return query;
    }

    private QueryModel buildQuery(Activity activity, FormTree formTree, FormClass form) {
        QueryModel query = new QueryModel(form.getId());
        fieldBindingMap.put(form.getId(), Lists.<FieldBinding>newLinkedList());

        addBinding(new SiteDimBinding(), query, formTree);
        addBinding(new ActivityIdFieldBinding(), query, formTree);
        addBinding(new ProjectDimBinding(), query, formTree);
        if (command.isFetchDates() && activity != null && !activity.isMonthly()) {
            addBinding(new StartEndDateFieldBinding(), query, formTree);
        }
        if (command.isFetchPartner()) {
            addBinding(new PartnerDimBinding(), query, formTree);
        }
        if (command.isFetchLocation()) {
            locationMap.put(formTree.getRootFormId(), new ArrayList<ResourceId>());
            query = buildLocationQuery(query, formTree, form);
        }
        if (command.isFetchAttributes()) {
            query = buildAttributeQuery(query, formTree, form);
        }
        if (command.fetchAnyIndicators()) {
            query = buildIndicatorQuery(query, formTree, form);
        }
        if (command.isFetchComments()) {
            addBinding(new CommentFieldBinding(), query, formTree);
        }

        return query;
    }

    private void addBinding(FieldBinding binding, QueryModel query, FormTree formTree) {
        query.addColumns(binding.getColumnQuery(formTree));
        fieldBindingMap.get(formTree.getRootFormId()).add(binding);
    }

    private QueryModel buildLocationQuery(QueryModel query, FormTree formTree, FormClass form) {
        switch (form.getId().getDomain()) {
            case CuidAdapter.ACTIVITY_DOMAIN:
                return addLocationField(query, formTree, form);
            case CuidAdapter.LOCATION_TYPE_DOMAIN:
                addGeoField(query, formTree, form);
                addAdminField(query, formTree, form, CuidAdapter.ADMIN_FIELD);
                return query;
            case CuidAdapter.ADMIN_LEVEL_DOMAIN:
                addBinding(new AdminEntityBinding(form), query, formTree);
                addAdminField(query, formTree, form, CuidAdapter.ADMIN_PARENT_FIELD);
                return query;
            default:
                // undefined location form...
                return query;
        }
    }

    private QueryModel addLocationField(QueryModel query, FormTree formTree, FormClass form) {
        FormField locationField = getField(form, CuidAdapter.field(form.getId(), CuidAdapter.LOCATION_FIELD));
        if (locationField != null) {
            Iterator<ResourceId> locationReferences = getRange(locationField);
            // Only build one location query
            ResourceId locationRef = locationReferences.next();
            addBinding(new LocationFieldBinding(locationRef), query, formTree);
            locationMap.get(formTree.getRootFormId()).add(locationRef);
            buildLocationQuery(query, formTree, formTree.getFormClass(locationRef));
        } else {
            // country form, get country instance from ActivityLoader
            CountryInstance country = getCountryInstance(form.getId());
            if (country != null) {
                addBinding(new CountryFieldBinding(country), query, formTree);
            }
        }
        return query;
    }

    private CountryInstance getCountryInstance(ResourceId locationFormId) {
        try {
            Activity activity = activities.get(CuidAdapter.getLegacyIdFromCuid(locationFormId));
            return catalog.getActivityLoader().loadCountryInstance(activity.getLocationTypeId());
        } catch (SQLException excp) {
            return null;
        }
    }

    private void addGeoField(QueryModel query, FormTree formTree, FormClass form) {
        FormField geoField = getField(form, CuidAdapter.field(form.getId(), CuidAdapter.GEOMETRY_FIELD));
        if (geoField != null) {
            if (geoField.getType() instanceof GeoPointType) {
                addBinding(new GeoPointFieldBinding(geoField), query, formTree);
            } else if (geoField.getType() instanceof GeoAreaType) {
                addBinding(new GeoAreaFieldBinding(form), query, formTree);
            }
        }
    }

    private void addAdminField(QueryModel query, FormTree formTree, FormClass form, int fieldIndex) {
        FormField adminField = getField(form, CuidAdapter.field(form.getId(), fieldIndex));
        if (adminField != null) {
            Iterator<ResourceId> adminRange = getRange(adminField);
            while (adminRange.hasNext()) {
                ResourceId adminEntityId = adminRange.next();
                if (!locationMap.get(formTree.getRootFormId()).contains(adminEntityId)) {
                    buildLocationQuery(query, formTree, formTree.getFormClass(adminEntityId));
                    locationMap.get(formTree.getRootFormId()).add(adminEntityId);
                }
            }
        }
    }

    private FormField getField(FormClass form, ResourceId fieldId) {
        try {
            return form.getField(fieldId);
        } catch (IllegalArgumentException excp) {
            return null;
        }
    }

    private Iterator<ResourceId> getRange(FormField field) {
        Collection<ResourceId> range = getRange(field.getType());
        if (range.isEmpty()) {
            throw new IllegalStateException("No form referenced on given field");
        }
        return range.iterator();
    }

    private Collection<ResourceId> getRange(FieldType type) {
        if (type instanceof ReferenceType) {
            ReferenceType refType = (ReferenceType) type;
            return refType.getRange();
        } else {
            throw new IllegalArgumentException("Given FieldType " + type + " should be of reference type");
        }
    }

    private QueryModel buildAttributeQuery(QueryModel query, FormTree formTree, FormClass activityForm) {
        for (FormField field : activityForm.getFields()) {
            if (field.getType() instanceof EnumType) {
                addBinding(new AttributeFieldBinding(field), query, formTree);
            }
        }
        return query;
    }

    private QueryModel buildIndicatorQuery(QueryModel query, FormTree formTree, FormClass activityForm) {
        if (command.isFetchAllIndicators()) {
            for (FormField field : activityForm.getFields()) {
                if (isDomain(field.getId(), CuidAdapter.INDICATOR_DOMAIN)) {
                    addBinding(new IndicatorFieldBinding(field), query, formTree);
                }
            }
        } else {
            for (Integer indicator : command.getFetchIndicators()) {
                ResourceId indicatorId = CuidAdapter.indicatorField(indicator);
                FormField indicatorField = getField(activityForm, indicatorId);
                if (indicatorField != null) {
                    addBinding(new IndicatorFieldBinding(indicatorField), query, formTree);
                }
            }
        }
        return query;
    }

    private boolean isDomain(ResourceId id, char domain) {
        Character idDomain = id.getDomain();
        return idDomain.equals(domain);
    }

}
