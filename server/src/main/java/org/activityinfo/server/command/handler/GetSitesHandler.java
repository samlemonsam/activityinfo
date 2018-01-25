package org.activityinfo.server.command.handler;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.data.SortInfo;
import com.google.cloud.trace.core.TraceContext;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.impl.OldGetSitesHandler;
import org.activityinfo.legacy.shared.model.*;
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
import org.activityinfo.store.hrd.AppEngineFormScanCache;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.server.util.Trace;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;
import org.activityinfo.store.mysql.metadata.CountryInstance;
import org.activityinfo.store.mysql.metadata.LinkedActivity;
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
    private Provider<MySqlStorageProvider> catalogProvider;

    @Inject
    private DispatcherSync dispatcher;

    private GetSites command;

    private MySqlStorageProvider catalog;
    private ColumnSetBuilder builder;
    private ColumnSetBuilder linkedBuilder;
    private BatchingFormTreeBuilder batchFormTreeBuilder;
    private FormScanBatch batch;
    private FormScanBatch linkedBatch;
    private SortInfo sortInfo;

    private Map<ResourceId,FormTree> formTreeMap;
    private Map<ResourceId,FormTree> linkedFormTreeMap;

    private Map<ResourceId,QueryModel> queryMap = new LinkedHashMap<>();
    private Map<ResourceId,List<FieldBinding>> fieldBindingMap = new HashMap<>();
    private List<Runnable> queryResultHandlers = new ArrayList<>();

    private Map<Integer,Activity> activities;
    private Map<Integer,Activity> linkedActivities;
    private Map<Integer,Activity> selfLinkedActivities;

    private int offset;
    private int limit;
    private int totalResultLength;

    private List<SiteDTO> siteList = Lists.newArrayList();
    private List<SiteDTO> monthlySiteList = Lists.newArrayList();
    private List<SiteDTO> linkedSiteList = Lists.newArrayList();
    private Map<ResourceId,SiteDTO> monthlyRootSiteMap = Maps.newHashMap();

    private Map<Integer, List<ActivityLink>> activityLinkMap = Maps.newHashMap();

    private final Stopwatch metadataTime = Stopwatch.createUnstarted();
    private final Stopwatch treeTime = Stopwatch.createUnstarted();
    private final Stopwatch queryBuildTime = Stopwatch.createUnstarted();
    private final Stopwatch queryExecTime = Stopwatch.createUnstarted();
    private final Stopwatch queryFetchTime = Stopwatch.createUnstarted();
    private final Stopwatch queryExtractTime = Stopwatch.createUnstarted();
    private final Stopwatch monthlyMergeTime = Stopwatch.createUnstarted();
    private final Stopwatch aggregateTime = Stopwatch.createUnstarted();

    public class ActivityLink {

        // Destination Id
        private final int activityId;

        // Source Id -> Destination Id
        private Map<Integer,List<Integer>> indicatorMap = Maps.newHashMap();
        private Map<Integer,List<Integer>> attributeMap = Maps.newHashMap();

        public ActivityLink(int activityId) {
            this.activityId = activityId;
        }

        public int getActivityId() {
            return activityId;
        }

        public Map<Integer,List<Integer>> getLinkedIndicators() {
            return indicatorMap;
        }

        public Map<Integer,List<Integer>> getLinkedAttributes() {
            return attributeMap;
        }

        public void buildIndicatorMap(Activity destinationActivity, LinkedActivity linkedActivity) {
            for (ActivityField field : destinationActivity.getAttributeAndIndicatorFields()) {
                if (field.isIndicator()) {
                    Iterator<Integer> indicatorLinks = linkedActivity.getSourceIndicatorIdsFor(field.getId()).iterator();
                    while (indicatorLinks.hasNext()) {
                        addToMap(indicatorMap, indicatorLinks.next(), field.getId());
                    }
                }
            }
        }

        private void addToMap(Map<Integer,List<Integer>> map, Integer key, Integer id) {
            if (!map.containsKey(key)) {
                map.put(key,Lists.<Integer>newLinkedList());
            }
            map.get(key).add(id);
        }

        public void buildAttributeMap(Activity destinationActivity, Activity sourceActivity) {
            Map<String,ActivityField> destinationAttributeFields;
            Map<String,ActivityField> sourceAttributeFields;

            destinationAttributeFields = extractAttributeFieldsWithLabels(destinationActivity);
            sourceAttributeFields = extractAttributeFieldsWithLabels(sourceActivity);

            for (Map.Entry<String, ActivityField> destinationField : destinationAttributeFields.entrySet()) {
                if (sourceAttributeFields.containsKey(destinationField.getKey())) {
                    Integer destinationId = destinationField.getValue().getId();
                    Integer sourceId = sourceAttributeFields.get(destinationField.getKey()).getId();
                    addToMap(attributeMap, sourceId, destinationId);
                }
            }
        }

        private Map<String,ActivityField> extractAttributeFieldsWithLabels(Activity activity) {
            Map<String,ActivityField> attributeFields = Maps.newHashMap();
            for (ActivityField field : activity.getAttributeAndIndicatorFields()) {
                if (field.isAttributeGroup()) {
                    attributeFields.put(field.getFormField().getName(),field);
                }
            }
            return attributeFields;
        }

    }

    private class SiteComparator implements Comparator<SiteDTO> {

        private SortInfo sortInfo;

        public SiteComparator(SortInfo sortInfo) {
            assert(sortInfo != null);
            this.sortInfo = sortInfo;
        }

        @Override
        public int compare(SiteDTO o1, SiteDTO o2) {
            assert(o1 != null);
            assert(o2 != null);

            if (sortInfo.getSortField() != null) {
                Object f1 = o1.get(sortInfo.getSortField());
                Object f2 = o2.get(sortInfo.getSortField());

                switch (sortInfo.getSortDir()) {
                    case ASC:
                        return compareFields(f1, f2);
                    case DESC:
                        return -compareFields(f1, f2);
                }
            }

            return 0;
        }

        private int compareFields(Object f1, Object f2) {
            if (f1 instanceof Integer && f2 instanceof Integer) {
                return ((Integer) f1).compareTo((Integer) f2);
            } else if (f1 instanceof LocalDate && f2 instanceof LocalDate) {
                return ((LocalDate) f1).compareTo((LocalDate) f2);
            } else if (f1 instanceof String && f2 instanceof String) {
                return ((String) f1).compareTo((String) f2);
            } else if (f1 instanceof Double && f2 instanceof Double) {
                return ((Double) f1).compareTo((Double) f2);
            } else if (f1 instanceof PartnerDTO && f2 instanceof PartnerDTO) {
                return ((PartnerDTO) f1).getName().compareTo(((PartnerDTO) f2).getName());
            } else if (f1 instanceof ProjectDTO && f2 instanceof ProjectDTO) {
                return ((ProjectDTO) f1).getName().compareTo(((ProjectDTO) f2).getName());
            } else {
                throw new CommandException("Unimplemented sort on GetSites");
            }
        }

    }

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
            constructActivityLinks();
            buildQueries();
            batchQueries();
            executeBatch();
            mergeMonthlyRootSites();
            setSitesLinkedStatus();
            sort();
        } catch (CommandException excp) {
            // If we catch a *Command* Exception, lets try the legacy method
            // Might want to strip this out after the robustness of the new method is established, as we could call the
            // old method at any point of execution and elongate the return time
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

    private boolean useLegacyMethod(GetSites command, User user) {
        return user == null
                || command.getFilter() == null
                || command.isLegacyFetch();
    }

    private void initialiseHandler(GetSites command, User user) {
        catalog = catalogProvider.get();
        if (catalog != null) {
            this.command = command;

            builder = new ColumnSetBuilder(catalog, new AppEngineFormScanCache(), new FormSupervisorAdapter(catalog, user.getId()));
            linkedBuilder = new ColumnSetBuilder(catalog, new AppEngineFormScanCache(), new NullFormSupervisor());
            batchFormTreeBuilder = new BatchingFormTreeBuilder(catalog);

            batch = builder.createNewBatch();
            linkedBatch = linkedBuilder.createNewBatch();

            selfLinkedActivities = Maps.newHashMap();

            sortInfo = command.getSortInfo();
            offset = command.getOffset();
            limit = command.getLimit();
            totalResultLength = 0;
        } else {
            throw new CommandException("Could not retrieve form catalog");
        }
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
            LOGGER.info("Fetching Activity Metadata for: " + Arrays.toString(filter.getRestrictions(DimensionType.Database).toArray()));
            return catalog.getActivityLoader().loadForDatabaseIds(filter.getRestrictions(DimensionType.Database));
        } else if (filter.isRestricted(DimensionType.Activity)) {
            LOGGER.info("Fetching Activity Metadata for: " + Arrays.toString(filter.getRestrictions(DimensionType.Activity).toArray()));
            return catalog.getActivityLoader().load(filter.getRestrictions(DimensionType.Activity));
        } else {
            throw new CommandException("Request too broad: must filter by Database or Activity");
        }
    }

    private void fetchLinkedActivityMetadata(List<Integer> linkedActivitiesToFetch) {
        TraceContext linkedActivityMetadataTrace = Trace.startSpan("ai/cmd/GetSites/fetchLinkedActivityMetadata");
        try {
            metadataTime.start();
            LOGGER.info("Fetching Linked Activity Metadata for: " + Arrays.toString(linkedActivitiesToFetch.toArray()));
            Filter linkedFilter = new Filter();
            linkedFilter.addRestriction(DimensionType.Activity, linkedActivitiesToFetch);
            linkedActivities = loadMetadata(linkedFilter);
        } catch (SQLException excp) {
            throw new CommandException("Could not fetch linked activity metadata from server");
        } finally {
            metadataTime.stop();
            Trace.endSpan(linkedActivityMetadataTrace);
        }
    }

    private void queryFormTrees() {
        TraceContext formTreeQueryTrace = Trace.startSpan("ai/cmd/GetSites/queryFormTrees");
        treeTime.start();

        Set<ResourceId> formIds = new HashSet<>();
        Set<ResourceId> linkedFormIds = new HashSet<>();
        List<Integer> linkedActivitiesToFetch = new ArrayList<>();
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
            if (command.isFetchLinks()) {
                for (LinkedActivity linkedActivity : activity.getLinkedActivities()) {
                    linkedActivitiesToFetch.add(linkedActivity.getActivityId());
                    linkedFormIds.add(CuidAdapter.activityFormClass(linkedActivity.getActivityId()));
                }
            }
        }

        formTreeMap = batchFormTreeBuilder.queryTrees(formIds);

        if (!linkedActivitiesToFetch.isEmpty()) {
            fetchLinkedActivityMetadata(linkedActivitiesToFetch);
            linkedFormTreeMap = batchFormTreeBuilder.queryTrees(linkedFormIds);
        }

        treeTime.stop();
        Trace.endSpan(formTreeQueryTrace);
    }

    private boolean reject(Activity activity) {
        return activity.isDeleted() || !activity.isClassicView();
    }

    private void constructActivityLinks() {
        if (!command.isFetchLinks()) {
            return;
        }
        for (Activity activity : activities.values()) {
            addSelfLink(activity);
            addExternalLinks(activity);
        }
    }

    private void addSelfLink(Activity activity) {
        if (!activity.getSelfLinkedIndicators().isEmpty()) {
            selfLinkedActivities.put(activity.getId(), activity);
        }
    }

    private void addExternalLinks(Activity activity) {
        Iterator<LinkedActivity> links = activity.getLinkedActivities().iterator();
        while (links.hasNext()) {
            ActivityLink activityLink = new ActivityLink(activity.getId());
            LinkedActivity linkedActivity = links.next();

            activityLink.buildIndicatorMap(activity, linkedActivity);
            activityLink.buildAttributeMap(activity, linkedActivities.get(linkedActivity.getActivityId()));

            addToLinkMap(activityLink, linkedActivity);
        }
    }

    private void addToLinkMap(ActivityLink activityLink, LinkedActivity linkedActivity) {
        if (!activityLinkMap.containsKey(linkedActivity.getActivityId())) {
            activityLinkMap.put(linkedActivity.getActivityId(), Lists.<ActivityLink>newLinkedList());
        }
        activityLinkMap.get(linkedActivity.getActivityId()).add(activityLink);
    }

    private void buildQueries() {
        TraceContext queryBuildTrace = Trace.startSpan("ai/cmd/GetSites/buildQuery");
        queryBuildTime.start();
        for (Map.Entry<ResourceId, FormTree> formTreeEntry : formTreeMap.entrySet()) {
            addToQueryMap(formTreeEntry, null);
        }
        if (command.isFetchLinks() && linkedFormTreeMap != null) {
            for (Map.Entry<ResourceId, FormTree> linkedFormTreeEntry : linkedFormTreeMap.entrySet()) {
                for (ActivityLink activityLink : activityLinkMap.get(CuidAdapter.getLegacyIdFromCuid(linkedFormTreeEntry.getKey()))) {
                    addToQueryMap(linkedFormTreeEntry, activityLink);
                }
            }
        }
        queryBuildTime.stop();
        Trace.endSpan(queryBuildTrace);
    }

    private void addToQueryMap(Map.Entry<ResourceId, FormTree> formTreeEntry, ActivityLink activityLink) {
        QueryModel query = buildQuery(formTreeEntry.getValue(), activityLink);
        query.setFilter(determineQueryFilter(command.getFilter(), formTreeEntry.getValue()));
        queryMap.put(formTreeEntry.getKey(), query);
        LOGGER.info(query.toString());
    }

    private ExprNode determineQueryFilter(Filter commandFilter, FormTree formTree) {
        QueryFilter queryFilter = new QueryFilter(commandFilter, HashMultimap.<String, String>create());
        return queryFilter.composeFilter(formTree);
    }

    private QueryModel buildQuery(FormTree formTree, ActivityLink activityLink) {
        if (activityLink != null) {
            return buildLinkedQuery(formTree, formTree.getRootFormClass(), activityLink);
        } else if (monthlyReportForm(formTree.getRootFormId())) {
            return buildMonthlyQuery(formTree, formTree.getRootFormClass());
        } else {
            Activity activity = activities.get(CuidAdapter.getLegacyIdFromCuid(formTree.getRootFormId()));
            return buildQuery(activity, formTree, formTree.getRootFormClass());
        }
    }

    private void addBinding(FieldBinding binding, QueryModel query, FormTree formTree) {
        query.addColumns(binding.getColumnQuery(formTree));
        fieldBindingMap.get(formTree.getRootFormId()).add(binding);
    }

    private QueryModel buildLinkedQuery(FormTree formTree, FormClass form, ActivityLink activityLink) {
        QueryModel query = new QueryModel(form.getId());
        fieldBindingMap.put(form.getId(), Lists.<FieldBinding>newLinkedList());

        addBinding(new SiteDimBinding(), query, formTree);
        addBinding(new ConstantActivityIdFieldBinding(activityLink.getActivityId()), query, formTree);
        addBinding(new ProjectDimBinding(), query, formTree);
        if (command.isFetchDates()) {
            addBinding(new StartEndDateFieldBinding(), query, formTree);
        }
        if (command.isFetchPartner()) {
            addBinding(new PartnerDimBinding(), query, formTree);
        }
        if (command.isFetchLocation()) {
            query = buildLocationQuery(query, formTree, form);
        }
        if (command.fetchAnyIndicators()) {
            query = buildLinkedIndicatorQuery(activityLink.getLinkedIndicators(), query, formTree, form);
        }
        if (command.isFetchAttributes()) {
            //query = buildLinkedAttributeQuery(activityLink.getLinkedAttributes(), query, formTree, form);
            query = buildAttributeQuery(query, formTree, form);
        }
        if (command.isFetchComments()) {
            addBinding(new CommentFieldBinding(), query, formTree);
        }

        return query;
    }

    private QueryModel buildLinkedIndicatorQuery(Map<Integer,List<Integer>> linkedIndicators, QueryModel query, FormTree formTree, FormClass form) {
        if (command.isFetchAllIndicators()) {
            for (Map.Entry<Integer, List<Integer>> linkedIndicator : linkedIndicators.entrySet()) {
                for (Integer destinationIndicator : linkedIndicator.getValue()) {
                    FormField indicatorField = getField(form, CuidAdapter.indicatorField(linkedIndicator.getKey()));
                    if (indicatorField != null) {
                        addLinkedIndicatorBinding(query, formTree, destinationIndicator, indicatorField);
                    }
                }
            }
        } else {
            for (Integer indicator : command.getFetchIndicators()) {
                ResourceId indicatorId = CuidAdapter.indicatorField(indicator);
                FormField indicatorField = getField(form, indicatorId);
                if (indicatorField != null) {
                    addLinkedIndicatorBinding(query, formTree, indicator, indicatorField);
                }
            }
        }

        return query;
    }

    private QueryModel buildLinkedAttributeQuery(Map<Integer,List<Integer>> linkedAttributes, QueryModel query, FormTree formTree, FormClass form) {
        if (command.isFetchAttributes()) {
            for (Map.Entry<Integer,List<Integer>> linkedAttribute : linkedAttributes.entrySet()) {
                for (Integer destinationAttribute : linkedAttribute.getValue()) {
                    addLinkedAttributeBinding(query,
                            formTree,
                            destinationAttribute,
                            form.getField(CuidAdapter.attributeId(linkedAttribute.getKey())));
                }
            }
        }
        return query;
    }

    private void addLinkedIndicatorBinding(QueryModel query, FormTree formTree, int destinationIndicator, FormField indicatorField) {
        FieldBinding linkedIndicatorBinding = new LinkedIndicatorFieldBinding(destinationIndicator, indicatorField);
        addBinding(linkedIndicatorBinding, query, formTree);
    }

    private void addLinkedAttributeBinding(QueryModel query, FormTree formTree, int destinationAttribute, FormField attributeField) {
        FieldBinding attributeFieldBinding = new LinkedAttributeFieldBinding(destinationAttribute, attributeField);
        addBinding(attributeFieldBinding, query, formTree);
    }

    private boolean monthlyReportForm(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS;
    }

    private QueryModel buildMonthlyQuery(FormTree formTree, FormClass form) {
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

    private QueryModel buildLocationQuery(QueryModel query, FormTree formTree, FormClass form) {
        Optional<FormField> potentialLocationField = form.getFieldIfPresent(CuidAdapter.field(form.getId(), CuidAdapter.LOCATION_FIELD));
        if (potentialLocationField.isPresent()) {
            FormField locationField = potentialLocationField.get();
            // Location binding will take care of the details wrt geo fields/admin field
            addBinding(new LocationFieldBinding(locationField), query, formTree);
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
            Activity activity;
            if (activities.containsKey(CuidAdapter.getLegacyIdFromCuid(locationFormId))) {
                activity = activities.get(CuidAdapter.getLegacyIdFromCuid(locationFormId));
            } else if (linkedActivities.containsKey(CuidAdapter.getLegacyIdFromCuid(locationFormId))){
                activity = linkedActivities.get(CuidAdapter.getLegacyIdFromCuid(locationFormId));
            } else {
                return null;
            }
            return catalog.getActivityLoader().loadCountryInstance(activity.getLocationTypeId());
        } catch (SQLException excp) {
            return null;
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

    private FormField getField(FormClass form, ResourceId fieldId) {
        try {
            return form.getField(fieldId);
        } catch (IllegalArgumentException excp) {
            return null;
        }
    }

    public static Iterator<ResourceId> getRange(FormField field) {
        Collection<ResourceId> range = getRange(field.getType());
        if (range.isEmpty()) {
            throw new IllegalStateException("No form referenced on given field");
        }
        return range.iterator();
    }

    public static Collection<ResourceId> getRange(FieldType type) {
        if (type instanceof ReferenceType) {
            ReferenceType refType = (ReferenceType) type;
            return refType.getRange();
        } else {
            throw new IllegalArgumentException("Given FieldType " + type + " should be of reference type");
        }
    }

    private boolean isDomain(ResourceId id, char domain) {
        Character idDomain = id.getDomain();
        return idDomain.equals(domain);
    }

    private void batchQueries() {
        for (final Map.Entry<ResourceId,QueryModel> queryEntry : queryMap.entrySet()) {
            enqueueQuery(
                    linkedForm(queryEntry.getKey()),
                    queryEntry.getValue(),
                    new Function<ColumnSet, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ColumnSet columnSet) {
                    ResourceId activityId = queryEntry.getKey();
                    List<FieldBinding> fieldBindings = fieldBindingMap.get(activityId);
                    List<SiteDTO> extractedSites;

                    if (linkedForm(activityId)) {
                        extractedSites = extractLinkedSites(fieldBindings, columnSet);
                        linkedSiteList.addAll(extractedSites);
                    } else {
                        if (monthlyReportForm(activityId)) {
                            extractedSites = extractLinkedSites(fieldBindings, columnSet);
                            monthlySiteList.addAll(extractedSites);
                        } else {
                            extractedSites = extractSites(fieldBindings, columnSet);
                            siteList.addAll(extractedSites);
                        }

                        if (selfLinkedActivities.containsKey(CuidAdapter.getLegacyIdFromCuid(activityId))) {
                            siteList.addAll(copySelfLinkedSites(extractedSites));
                        }
                    }
                    return null;
                }
            });
        }
    }

    private boolean linkedForm(ResourceId formId) {
        return linkedFormTreeMap != null && linkedFormTreeMap.containsKey(formId);
    }

    private void enqueueQuery(boolean linked, QueryModel query, final Function<ColumnSet,Void> handler) {
        final Slot<ColumnSet> result = linked ? linkedBuilder.enqueue(query,linkedBatch) : builder.enqueue(query,batch);
        queryResultHandlers.add(new Runnable() {
            @Override
            public void run() {
                ColumnSet columnSet = result.get();
                handler.apply(columnSet);
            }
        });
    }

    private List<SiteDTO> extractSites(List<FieldBinding> fieldBindings, ColumnSet columnSet) {
        if (acceptResult(columnSet.getNumRows())) {
            SiteDTO[] sites;
            if (command.isFetchAllReportingPeriods()) {
                sites = extractSiteData(fieldBindings, columnSet);
                addMonthlyRootSites(sites);
                return Collections.emptyList();
            } else {
                totalResultLength = totalResultLength + columnSet.getNumRows();
                sites = extractSiteData(fieldBindings, columnSet);
                return Lists.newArrayList(sites);
            }
        }
        return Collections.emptyList();
    }

    private List<SiteDTO> copySelfLinkedSites(List<SiteDTO> sites) {
        List<SiteDTO> selfLinkedSites = new ArrayList<SiteDTO>(sites.size());

        for (SiteDTO site : sites) {
            SiteDTO selfLinkCopy = new SiteDTO(site);

            // Strip of indicator and attribute properties - except for linked indicators
            for (String property : site.getPropertyNames()) {
                if (property.startsWith(AttributeDTO.PROPERTY_PREFIX) || property.startsWith(IndicatorDTO.PROPERTY_PREFIX)) {
                    selfLinkCopy.remove(property);
                }
            }

            linkedSiteList.add(selfLinkCopy);
            selfLinkedSites.add(selfLinkCopy);
        }
        return selfLinkedSites;
    }

    private List<SiteDTO> extractLinkedSites(List<FieldBinding> fieldBindings, ColumnSet columnSet) {
        if (acceptResult(columnSet.getNumRows())) {
            SiteDTO[] extractedSiteArray = extractSiteData(fieldBindings, columnSet);
            List<SiteDTO> extractedSiteList = Lists.newArrayList(extractedSiteArray);
            siteList.addAll(extractedSiteList);
            return extractedSiteList;
        }
        return Collections.emptyList();
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

    private void executeBatch() {
        TraceContext batchExecutionTrace = Trace.startSpan("ai/cmd/GetSites/executeBatch");
        try {
            queryExecTime.start();

            TraceContext fetchTrace = Trace.startSpan("ai/cmd/GetSites/executeBatch/fetchColumns");
            queryFetchTime.start();
            builder.execute(batch);
            linkedBuilder.execute(linkedBatch);
            queryFetchTime.stop();
            Trace.endSpan(fetchTrace);
        } catch (Exception excp) {
            throw new RuntimeException("Failed to execute query batch", excp);
        }

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
        for (SiteDTO monthlySite : monthlySiteList) {
            if (monthlyRootSiteMap.containsKey(monthlySite.getInstanceId())) {
                SiteDTO rootSite = monthlyRootSiteMap.get(monthlySite.getInstanceId());
                monthlySite.setLocation(rootSite.getLocation());
                monthlySite.setProject(rootSite.getProject());
                monthlySite.setPartner(rootSite.getPartner());
            }
        }
        monthlyMergeTime.stop();
        Trace.endSpan(monthlyMergeTrace);
    }

    private void setSitesLinkedStatus() {
        for (SiteDTO linkedSite : linkedSiteList) {
            linkedSite.setLinked(true);
        }
    }

    private void sort() {
        if (sortInfo != null && !siteList.isEmpty()) {
            TraceContext sortTrace = Trace.startSpan("ai/cmd/GetSites/executeBatch/sortSites");
            SiteComparator comparator = new SiteComparator(sortInfo);
            Collections.sort(siteList, comparator);
            Trace.endSpan(sortTrace);
        }
    }

    // Sorting on QueryEngine level
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

    private void printTimes() {
        LOGGER.info("GetSites timings: {" + "Metadata Fetch: " + metadataTime.toString() + "; " +
                "Form Tree Fetch: " + treeTime.toString() + "; " +
                "Query Build: " + queryBuildTime.toString() + "; " +
                "Query Column Fetch: " + queryFetchTime.toString() + "; " +
                "Query Result Extraction: " + queryExtractTime.toString() + "; " +
                "Query Total Execution Time: " + queryExecTime.toString() + "; " +
                "Monthly Indicator Merge: " + monthlyMergeTime.toString() + "; " +
                "Aggregate Time: " + aggregateTime.toString()
        + "}");
    }

}
