/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command.handler;

import com.extjs.gxt.ui.client.data.SortInfo;
import com.google.cloud.trace.core.TraceContext;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.OldGetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.impl.OldGetSitesHandler;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.command.handler.binding.*;
import org.activityinfo.server.command.handler.binding.dim.PartnerDimBinding;
import org.activityinfo.server.command.handler.binding.dim.ProjectDimBinding;
import org.activityinfo.server.command.handler.binding.dim.SiteDimBinding;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.util.Trace;
import org.activityinfo.store.hrd.AppEngineFormScanCache;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.CountryInstance;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.FormSupervisorAdapter;
import org.activityinfo.store.query.shared.FormScanBatch;
import org.activityinfo.store.spi.BatchingFormTreeBuilder;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.Slot;

import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetSitesHandler implements CommandHandler<GetSites> {

    private static final Logger LOGGER = Logger.getLogger(GetSitesHandler.class.getName());

    @Inject
    private Provider<MySqlStorageProvider> catalogProvider;

    @Inject
    private DispatcherSync dispatcher;

    @Inject
    private DatabaseProvider databaseProvider;

    private GetSites command;

    private MySqlStorageProvider catalog;
    private ColumnSetBuilder builder;
    private BatchingFormTreeBuilder batchFormTreeBuilder;
    private FormScanBatch batch;
    private SortInfo sortInfo;

    private Map<ResourceId,FormTree> formTreeMap;
    private AttributeFilterMap attributeFilters;
    private Map<ResourceId,QueryModel> queryMap = new LinkedHashMap<>();
    private Map<ResourceId,List<FieldBinding>> fieldBindingMap = new HashMap<>();
    private List<Runnable> queryResultHandlers = new ArrayList<>();

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
    private final Stopwatch queryFetchTime = Stopwatch.createUnstarted();
    private final Stopwatch queryExtractTime = Stopwatch.createUnstarted();
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
            checkForLinkedActivities();
            queryFormTrees();
            buildAttributeFilterMap();
            buildQueries();
            setQuerySort();
            batchQueries();
            executeBatch();
            mergeMonthlyRootSites();
        } catch (CommandException excp) {
            // If we catch a CommandException, lets try the legacy method
            // TODO: Strip this out once robustness is established, and the Linked Indicator feature is disabled
            return dispatcher.execute(new OldGetSites(cmd));
        }

        aggregateTime.stop();
        printTimes();

        SiteResult result = new SiteResult(siteList);
        result.setOffset(cmd.getOffset());
        result.setTotalLength(totalResultLength);

        LOGGER.info("Exiting execute()");
        return result;
    }

    private void buildAttributeFilterMap() {
        attributeFilters = new AttributeFilterMap(command.getFilter(), formTreeMap.values());
    }

    private boolean useLegacyMethod(GetSites command, User user) {
        return user == null
                || command.getFilter() == null
                || command.isLegacyFetch();
    }

    private void initialiseHandler(GetSites command, User user) {
        catalog = catalogProvider.get();

        if (catalog == null) {
            throw new CommandException("Could not retrieve form catalog");
        }

        this.command = command;

        builder = new ColumnSetBuilder(catalog,
                new AppEngineFormScanCache(),
                new FormSupervisorAdapter(catalog, databaseProvider, user.getId()));
        batchFormTreeBuilder = new BatchingFormTreeBuilder(catalog);

        batch = builder.createNewBatch();

        sortInfo = command.getSortInfo();
        offset = command.getOffset();
        limit = command.getLimit();
        totalResultLength = 0;
    }

    private void fetchActivityMetadata(Filter filter) {
        TraceContext activityMetadataTrace = Trace.startSpan("ai/cmd/GetSites/fetchActivityMetadata");
        try {
            metadataTime.start();
            activities = loadMetadata(filter);
        } catch (SQLException excp) {
            throw new CommandException("Could not fetch metadata from server");
        } finally {
            metadataTime.stop();
            Trace.endSpan(activityMetadataTrace);
        }
    }

    private Map<Integer,Activity> loadMetadata(Filter filter) throws SQLException {
        if (filter.isRestricted(DimensionType.Database)) {
            LOGGER.log(Level.INFO, "Fetching Activity Metadata for: {0}", Arrays.toString(filter.getRestrictions(DimensionType.Database).toArray()));
            return catalog.getActivityLoader().loadForDatabaseIds(filter.getRestrictions(DimensionType.Database));
        } else if (filter.isRestricted(DimensionType.Activity)) {
            LOGGER.log(Level.INFO, "Fetching Activity Metadata for: {0}", Arrays.toString(filter.getRestrictions(DimensionType.Activity).toArray()));
            return catalog.getActivityLoader().load(filter.getRestrictions(DimensionType.Activity));
        } else {
            throw new CommandException("Request too broad: must filter by Database or Activity");
        }
    }

    private void checkForLinkedActivities() {
        activities.values().forEach(activity -> {
            if (!activity.getLinkedActivities().isEmpty() || !activity.getSelfLinkedIndicators().isEmpty()) {
                throw new CommandException("Linked Activity - Run OldGetSites");
            }
        });
    }

    private void queryFormTrees() {
        TraceContext formTreeQueryTrace = Trace.startSpan("ai/cmd/GetSites/queryFormTrees");
        treeTime.start();

        Set<ResourceId> formIds = new HashSet<>();

        for (Activity activity : activities.values()) {
            if (reject(activity)) {
                continue;
            }
            formIds.add(activity.getSiteFormClassId());
            if (activity.isMonthly() && command.isFetchAllReportingPeriods()) {
                formIds.add(CuidAdapter.reportingPeriodFormClass(activity.getId()));
            }
        }

        formTreeMap = batchFormTreeBuilder.queryTrees(formIds);

        treeTime.stop();
        Trace.endSpan(formTreeQueryTrace);
    }

    private boolean reject(Activity activity) {
        return activity.isDeleted() || !activity.isClassicView();
    }

    private void buildQueries() {
        TraceContext queryBuildTrace = Trace.startSpan("ai/cmd/GetSites/buildQuery");
        queryBuildTime.start();

        formTreeMap.forEach((formId,formTree) -> {
            QueryModel query = buildQuery(formId, formTree);
            query.setFilter(determineQueryFilter(command.getFilter(), formTree));
            queryMap.put(formId, query);
            LOGGER.info(query.toString());
        });

        queryBuildTime.stop();
        Trace.endSpan(queryBuildTrace);
    }

    private FormulaNode determineQueryFilter(Filter commandFilter, FormTree formTree) {
        QueryFilterBuilder queryFilter = new QueryFilterBuilder(commandFilter, attributeFilters);
        return queryFilter.composeFilter(formTree);
    }

    private QueryModel buildQuery(ResourceId formId, FormTree formTree) {
        if (monthlyReportForm(formId)) {
            return buildMonthlyQuery(formTree, formTree.getRootFormClass());
        } else {
            Activity activity = activities.get(CuidAdapter.getLegacyIdFromCuid(formId));
            return buildQuery(activity, formTree, formTree.getRootFormClass());
        }
    }

    private boolean monthlyReportForm(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS;
    }

    private void addBinding(FieldBinding binding, QueryModel query, FormTree formTree) {
        query.addColumns(binding.getColumnQuery(formTree));
        fieldBindingMap.get(formTree.getRootFormId()).add(binding);
    }

    private QueryModel buildMonthlyQuery(@NotNull FormTree formTree, @NotNull FormClass form) {
        QueryModel query = new QueryModel(form.getId());
        fieldBindingMap.put(form.getId(), Lists.newLinkedList());

        addBinding(new SiteDimBinding(), query, formTree);
        addBinding(new ActivityIdFieldBinding(), query, formTree);
        addBinding(new StartEndDateFieldBinding(), query, formTree);
        if (command.isFetchAttributes()) {
            buildAttributeQuery(query, formTree, form);
        }
        if (command.fetchAnyIndicators()) {
            buildIndicatorQuery(query, formTree, form);
        }
        return query;
    }

    private QueryModel buildQuery(@NotNull Activity activity, @NotNull FormTree formTree, @NotNull FormClass form) {
        QueryModel query = new QueryModel(form.getId());
        fieldBindingMap.put(form.getId(), Lists.newLinkedList());

        addBinding(new SiteDimBinding(), query, formTree);
        addBinding(new ActivityIdFieldBinding(), query, formTree);
        addBinding(new ProjectDimBinding(), query, formTree);
        if (command.isFetchDates() && !activity.isMonthly()) {
            addBinding(new StartEndDateFieldBinding(), query, formTree);
        }
        if (command.isFetchPartner()) {
            addBinding(new PartnerDimBinding(), query, formTree);
        }
        if (command.isFetchLocation()) {
            buildLocationQuery(query, formTree, form);
        }
        if (command.isFetchAttributes()) {
            buildAttributeQuery(query, formTree, form);
        }
        if (command.fetchAnyIndicators()) {
            buildIndicatorQuery(query, formTree, form);
        }
        if (command.isFetchComments()) {
            addBinding(new CommentFieldBinding(), query, formTree);
        }

        return query;
    }

    private void buildLocationQuery(QueryModel query, FormTree formTree, FormClass form) {
        Optional<FormField> locationField = form.getFieldIfPresent(CuidAdapter.field(form.getId(), CuidAdapter.LOCATION_FIELD));
        if (locationField.isPresent()) {
            addBinding(new LocationFieldBinding(locationField.get()), query, formTree);
            return;
        }

        // If no location field present, then it is a country form - get country instance from ActivityLoader
        Optional<CountryInstance> country = getCountryInstance(form.getId());
        if (country.isPresent()) {
            addBinding(new CountryFieldBinding(country.get()), query, formTree);
            return;
        }
    }

    private Optional<CountryInstance> getCountryInstance(ResourceId destinationFormId) {
        try {
            Activity activity = activities.get(CuidAdapter.getLegacyIdFromCuid(destinationFormId));
            return Optional.of(catalog.getActivityLoader().loadCountryInstance(activity.getLocationTypeId()));
        } catch (SQLException exception) {
            return Optional.absent();
        }
    }

    private void buildAttributeQuery(QueryModel query, FormTree formTree, FormClass form) {
        form.getFields().forEach(field -> {
            if (field.getType() instanceof EnumType) {
                addBinding(new AttributeFieldBinding(field), query, formTree);
            }
        });
    }

    private void buildIndicatorQuery(QueryModel query, FormTree formTree, FormClass form) {
        if (command.isFetchAllIndicators()) {
            form.getFields().forEach(field -> {
                if (field.getId().getDomain() == CuidAdapter.INDICATOR_DOMAIN) {
                    addBinding(new IndicatorFieldBinding(field), query, formTree);
                }
            });
        } else {
            command.getFetchIndicators().forEach(id -> {
                ResourceId indicatorId = CuidAdapter.indicatorField(id);
                Optional<FormField> field = form.getFieldIfPresent(indicatorId);
                if (field.isPresent()) {
                    addBinding(new IndicatorFieldBinding(field.get()), query, formTree);
                }
            });
        }
    }

    public static Iterator<ResourceId> getRange(FormField field) {
        Collection<ResourceId> range = getRange(field.getType());
        if (range.isEmpty()) {
            LOGGER.log(Level.WARNING, "No form(s) referenced on field {0}", field.getId());
            return Collections.emptyIterator();
        }
        return range.iterator();
    }

    public static Collection<ResourceId> getRange(FieldType type) {
        if (!(type instanceof ReferenceType)) {
            throw new IllegalArgumentException("Given FieldType " + type + " should be of reference type");
        }

        ReferenceType refType = (ReferenceType) type;
        return refType.getRange();
    }

    private void batchQueries() {
        queryMap.forEach((formId, query) ->
            enqueueQuery(query, columnSet -> {
                List<FieldBinding> fieldBindings = fieldBindingMap.get(formId);

                if (monthlyReportForm(formId)) {
                    monthlySiteList.addAll(extractMonthlySites(fieldBindings, columnSet));
                } else {
                    siteList.addAll(extractSites(fieldBindings, columnSet));
                }

                return null;
            })
        );
    }

    private void enqueueQuery(QueryModel query, final Function<ColumnSet,Void> handler) {
        final Slot<ColumnSet> result = builder.enqueue(query,batch);
        queryResultHandlers.add(() -> handler.apply(result.get()));
    }

    private List<SiteDTO> extractMonthlySites(List<FieldBinding> fieldBindings, ColumnSet columnSet) {
        if (acceptResult(columnSet.getNumRows())) {
            totalResultLength = totalResultLength + columnSet.getNumRows();
            SiteDTO[] extractedSiteArray = extractSiteData(fieldBindings, columnSet, false);
            List<SiteDTO> extractedSiteList = Lists.newArrayList(extractedSiteArray);
            siteList.addAll(extractedSiteList);
            return extractedSiteList;
        }
        return Collections.emptyList();
    }

    private List<SiteDTO> extractSites(List<FieldBinding> fieldBindings, ColumnSet columnSet) {
        SiteDTO[] sites;
        if (command.isFetchAllReportingPeriods()) {
            // If we are fetching all reporting periods, then these sites are roots for our monthly sites and do not
            // contribute to the total site count
            sites = extractSiteData(fieldBindings, columnSet, true);
            addMonthlyRootSites(sites);
            return Collections.emptyList();
        } else if (acceptResult(columnSet.getNumRows())) {
            // Otherwise these are normal sites and should count towards the total site count
            totalResultLength = totalResultLength + columnSet.getNumRows();
            sites = extractSiteData(fieldBindings, columnSet, false);
            return Lists.newArrayList(sites);
        } else {
            return Collections.emptyList();
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

    private SiteDTO[] extractSiteData(List<FieldBinding> fieldBindings, ColumnSet columnSet, boolean skipPagination) {
        ColumnSet finalColumnSet;
        SiteDTO[] sites;

        if (!skipPagination && (offset > 0 || limit > 0)) {
            Map<String,ColumnView> paginatedColumns = Maps.newHashMap();
            int[] index = generatePaginationIndex(columnSet.getNumRows());
            sites = initialiseSiteArray(index.length);
            columnSet.getColumns().forEach((id,column) -> paginatedColumns.put(id,column.select(index)));
            finalColumnSet = new ColumnSet(index.length, paginatedColumns);
        } else {
            sites = initialiseSiteArray(columnSet.getNumRows());
            finalColumnSet = columnSet;
        }

        for (FieldBinding binding : fieldBindings) {
            binding.extractFieldData(sites, finalColumnSet);
        }

        return sites;
    }

    private SiteDTO[] initialiseSiteArray(int length) {
        SiteDTO[] array = new SiteDTO[length];
        for (int i=0; i<array.length; i++) {
            array[i] = new SiteDTO();
        }
        return array;
    }

    private int[] generatePaginationIndex(int numResultRows) {
        int pageOffset = (offset > 0) ? offset : 0;
        int pageLimit;
        int pageSize;

        // Determine the current page limit if a limit is set
        if (limit > 0) {
            // if numResults is smaller than the remaining limit, then use numResults as current page limit
            if (numResultRows < (limit-siteList.size())) {
                pageLimit = numResultRows;
            } else {
                pageLimit = limit - siteList.size();
            }
        } else {
            pageLimit = numResultRows;
        }

        // If the number of sites remaining after offset is less than the current page limit, use that as the page size
        if (pageLimit < (numResultRows-pageOffset)) {
            pageSize = pageLimit;
        } else {
            pageSize = numResultRows-pageOffset;
        }

        int[] pageIndex = new int[pageSize];
        for (int i=0; i<pageIndex.length; i++) {
            pageIndex[i] = pageOffset + i;
        }

        offset = offset - pageLimit;
        return pageIndex;
    }

    private void addMonthlyRootSites(SiteDTO[] sites) {
        for (SiteDTO site : sites) {
            monthlyRootSiteMap.put(site.getInstanceId(), site);
        }
    }

    private void executeBatch() {
        TraceContext batchExecutionTrace = Trace.startSpan("ai/cmd/GetSites/executeBatch");
        queryExecTime.start();

        TraceContext fetchTrace = Trace.startSpan("ai/cmd/GetSites/executeBatch/fetchColumns");
        queryFetchTime.start();
        builder.execute(batch);
        queryFetchTime.stop();
        Trace.endSpan(fetchTrace);

        TraceContext dataExtractionTrace = Trace.startSpan("ai/cmd/GetSites/executeBatch/extractColumnData");
        queryExtractTime.start();
        for (Runnable handler : queryResultHandlers) {
            handler.run();
        }
        queryExtractTime.stop();
        Trace.endSpan(dataExtractionTrace);

        queryExecTime.stop();
        Trace.endSpan(batchExecutionTrace);
    }

    private void mergeMonthlyRootSites() {
        TraceContext monthlyMergeTrace = Trace.startSpan("ai/cmd/GetSites/executeBatch/mergeMonthlySites");
        monthlyMergeTime.start();

        monthlySiteList.forEach(monthlySite -> {
            if (monthlyRootSiteMap.containsKey(monthlySite.getInstanceId())) {
                SiteDTO rootSite = monthlyRootSiteMap.get(monthlySite.getInstanceId());
                monthlySite.setLocation(rootSite.getLocation());
                monthlySite.setProject(rootSite.getProject());
                monthlySite.setPartner(rootSite.getPartner());
            }
        });

        monthlyMergeTime.stop();
        Trace.endSpan(monthlyMergeTrace);
    }

    private void setQuerySort() {
        if (sortInfo == null) {
            return;
        }

        SortModel sortModel;
        switch(sortInfo.getSortDir()) {
            case ASC:
                sortModel = new SortModel(parseSortColumn(sortInfo.getSortField()), SortDir.ASC);
                break;
            case DESC:
            default:
                sortModel = new SortModel(parseSortColumn(sortInfo.getSortField()), SortDir.DESC);
                break;
        }

        queryMap.values().forEach(query -> query.addSortModel(sortModel));
    }

    // Transform from SortInfo fields to QueryEngine columns
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
        } else if (sortField.equals("partner.name")) {
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
            return (new CompoundExpr(new SymbolNode(adminLevelId), LocationFieldBinding.NAME_SYMBOL)).toString();
        } else {
            LOGGER.log(Level.WARNING,"Unimplemented sort on GetSites: '{0}'", sortField);
            return null;
        }
    }

    private void printTimes() {
        LOGGER.log(Level.INFO,() ->
            "GetSites timings: {" + "Metadata Fetch: " + metadataTime.toString() + "; " +
                    "Form Tree Fetch: " + treeTime.toString() + "; " +
                    "Query Build: " + queryBuildTime.toString() + "; " +
                    "Query Column Fetch: " + queryFetchTime.toString() + "; " +
                    "Query Result Extraction: " + queryExtractTime.toString() + "; " +
                    "Query Total Execution Time: " + queryExecTime.toString() + "; " +
                    "Monthly Indicator Merge: " + monthlyMergeTime.toString() + "; " +
                    "Aggregate Time: " + aggregateTime.toString()
                    + "}"
        );
    }

}
