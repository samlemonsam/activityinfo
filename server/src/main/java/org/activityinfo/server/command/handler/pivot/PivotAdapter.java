package org.activityinfo.server.command.handler.pivot;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.*;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.AndFunction;
import org.activityinfo.model.expr.functions.GreaterOrEqualFunction;
import org.activityinfo.model.expr.functions.LessOrEqualFunction;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.service.store.BatchingFormTreeBuilder;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.MySqlSession;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;
import org.activityinfo.store.mysql.metadata.LinkedActivity;
import org.activityinfo.store.mysql.metadata.UserPermission;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Collections.*;
import static org.activityinfo.model.expr.Exprs.*;
import static org.activityinfo.model.legacy.CuidAdapter.partnerInstanceId;

/**
 * Executes a legacy PivotSites query against the new API
 */
public class PivotAdapter {

    private static final Logger LOGGER = Logger.getLogger(PivotAdapter.class.getName());
    public static final String SITE_ID_KEY = "__site_id";

    private final MySqlSession session;
    private final PivotSites command;
    private final Filter filter;
    private final int userId;

    /**
     * Maps indicator ids to form class ids
     */
    private final List<Activity> activities;
    private final Multimap<Integer, Activity> databases = HashMultimap.create();

    private Map<ResourceId, FormTree> formTrees;

    private List<DimBinding> groupBy;

    private Optional<IndicatorDimBinding> indicatorDimension = Optional.absent();

    private Multimap<String, String> attributeFilters = HashMultimap.create();

    private final Map<Object, Accumulator> buckets = Maps.newHashMap();

    private final Stopwatch metadataTime = Stopwatch.createUnstarted();
    private final Stopwatch treeTime = Stopwatch.createUnstarted();
    private final Stopwatch queryTime = Stopwatch.createUnstarted();
    private final Stopwatch aggregateTime = Stopwatch.createUnstarted();


    public PivotAdapter(CollectionCatalog catalog, PivotSites command, int userId) throws InterruptedException, SQLException {
        this.session = (MySqlSession) catalog;
        this.command = command;
        this.filter = command.getFilter();
        this.userId = userId;


        // Mapping from indicator id -> activityId
        metadataTime.start();
        activities = Lists.newArrayList(loadMetadata().values());
        for (Activity activity : activities) {
            databases.put(activity.getDatabaseId(), activity);
        }
        metadataTime.stop();

        // Query form trees: needed to determine attribute mapping
        formTrees = queryFormTrees();

        findAttributeFilterNames();

        // Define the dimensions we're pivoting by
        groupBy = new ArrayList<>();
        for (Dimension dimension : command.getDimensions()) {
            if(dimension.getType() == DimensionType.Indicator) {
                indicatorDimension = Optional.of(new IndicatorDimBinding());
            } else {
                groupBy.add(bindingFor(dimension));
            }
        }
    }
    
    private Map<Integer, Activity> loadMetadata() throws SQLException {
        if(filter.isRestricted(DimensionType.Activity)) {
            return session.getActivityLoader().load(filter.getRestrictions(DimensionType.Activity));
        } else if(filter.isRestricted(DimensionType.Database)) {
            return session.getActivityLoader().loadForDatabaseIds(filter.getRestrictions(DimensionType.Database));
        } else if(filter.isRestricted(DimensionType.Indicator)) {
            return session.getActivityLoader().loadForIndicators(filter.getRestrictions(DimensionType.Indicator));
        }
        
        throw new CommandException("Filter too broad: must filter by database, activity, or indicator");
    }

    private Map<ResourceId, FormTree> queryFormTrees() {

        treeTime.start();

        Set<ResourceId> formIds = new HashSet<>();
        for(Activity activity : activities) {
            formIds.add(activity.getLeafFormClassId());
            for (LinkedActivity linkedActivity : activity.getLinkedActivities()) {
                formIds.add(linkedActivity.getLeafFormClassId());
            }
        }

        BatchingFormTreeBuilder builder = new BatchingFormTreeBuilder(session);
        Map<ResourceId, FormTree> trees = builder.queryTrees(formIds);

        treeTime.stop();

        return trees;

    }

    /**
     * Maps attribute filter ids to their attribute group id and name.
     *
     * Attribute filters are _SERIALIZED_ as only the integer ids of the required attributes,
     * but they are actually applied by _NAME_ to all forms in the query.
     *
     */
    private void findAttributeFilterNames() {

        Set<Integer> attributeIds = filter.getRestrictions(DimensionType.Attribute);
        if(attributeIds.isEmpty()) {
            return;
        }

        for (FormTree formTree : formTrees.values()) {
            for (FormTree.Node node : formTree.getLeaves()) {
                if(node.isEnum()) {
                    EnumType type = (EnumType) node.getType();
                    for (EnumItem enumItem : type.getValues()) {
                        int attributeId = CuidAdapter.getLegacyIdFromCuid(enumItem.getId());
                        if(attributeIds.contains(attributeId)) {
                            attributeFilters.put(node.getField().getLabel(), enumItem.getLabel());
                            attributeIds.remove(attributeId);
                            if(attributeIds.isEmpty()) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }


    private DimBinding bindingFor(Dimension dimension) {
        switch (dimension.getType()) {
            case Partner:
                return new PartnerDimBinding();
            case Project:
                return new ProjectDimBinding();

            case Activity:
                return new ActivityDimBinding();
            case ActivityCategory:
                return new ActivityCategoryDimBinding();

            case Date:
                return new DateDimBinding((DateDimension) dimension);

            case AttributeGroup:
                return new AttributeDimBinding((AttributeGroupDimension) dimension, formTrees.values());

            case AdminLevel:
                return new AdminDimBinding((AdminDimension) dimension);

            case Location:
                return new LocationDimBinding();

            case Site:
                return new SiteDimBinding();

            case Database:
                return new DatabaseDimBinding();
            
            case Target:
                return new TargetDimBinding();

            case Status:
            case Attribute:
                break;
        }
        throw new UnsupportedOperationException("Unsupported dimension " + dimension);
    }

    public PivotSites.PivotResult execute() throws SQLException {

        for (Activity activity : activities) {
            UserPermission userPermission = session.getActivityLoader().getPermission(activity.getId(), userId);
            if(userPermission.isView()) {
                Optional<ExprNode> permissionFilter = permissionFilter(userPermission);
                switch (command.getValueType()) {
                    case INDICATOR:
                        executeIndicatorValuesQuery(activity, activity.getSelfLink(), permissionFilter);
                        for (LinkedActivity linkedActivity : activity.getLinkedActivities()) {
                            executeIndicatorValuesQuery(activity, linkedActivity, permissionFilter);
                        }
                        break;
                    case TOTAL_SITES:
                        executeSiteCountQuery(activity, activity.getSelfLink(), permissionFilter);
                        for (LinkedActivity linkedActivity : activity.getLinkedActivities()) {
                            executeSiteCountQuery(activity, linkedActivity, permissionFilter);
                        }
                        break;
                }
            }
        }

        if(command.isPivotedBy(DimensionType.Target) &&
           command.getValueType() == PivotSites.ValueType.INDICATOR) {
           
            for (Integer databaseId : databases.keySet()) {
                executeTargetValuesQuery(databaseId);
            }
        }

        LOGGER.info(String.format("Pivot timings: metadata %s, trees: %s, query: %s, aggregate: %s",
                metadataTime, treeTime, queryTime, aggregateTime));


        return new PivotSites.PivotResult(createBuckets());
    }

    private Optional<ExprNode> permissionFilter(UserPermission userPermission) {
        if(userPermission.isViewAll()) {
            return Optional.absent();
        } else {
            ExprNode partnerFilter = Exprs.equals(
                    new SymbolExpr("partner"), 
                    new ConstantExpr(partnerInstanceId(userPermission.getPartnerId()).asString()));
            
            return Optional.of(partnerFilter);
        }
    }


    private List<ActivityField> selectedIndicators(Activity activity) {
        if(filter.isRestricted(DimensionType.Activity)) {
            if(!filter.getRestrictions(DimensionType.Activity).contains(activity.getId())) {
                return Collections.emptyList();
            }
        }
        if(filter.isRestricted(DimensionType.Database)) {
            if(!filter.getRestrictions(DimensionType.Database).contains(activity.getDatabaseId())) {
                return Collections.emptyList();
            }
        }
        List<ActivityField> matching = Lists.newArrayList();
        Set<Integer> restrictedIndicatorIds = filter.getRestrictions(DimensionType.Indicator);
        for (ActivityField field : activity.getIndicatorFields()) {
            FieldType type = field.getFormField().getType();
            if(type instanceof QuantityType || type instanceof CalculatedFieldType) {
                if(restrictedIndicatorIds.isEmpty() || restrictedIndicatorIds.contains(field.getId())) {
                    matching.add(field);
                }
            }
        }
        
        return matching;
    }

    private void executeIndicatorValuesQuery(Activity activity, 
                                             LinkedActivity linkedActivity, 
                                             Optional<ExprNode> permissionFilter) throws SQLException {
        
        // Double check that this activity has not been deleted
        if(isDeleted(activity, linkedActivity)) {
            return;
        }
        
        // Query the SOURCE form tree
        FormTree formTree = formTrees.get(linkedActivity.getLeafFormClassId());
        QueryModel queryModel = new QueryModel(linkedActivity.getLeafFormClassId());

        List<ActivityField> indicators = selectedIndicators(activity);
        
        
        // Add Indicators to the query
        // Keep track of alias to destination map
        Multimap<String, ActivityField> aliasToIndicator = HashMultimap.create();
        for (ActivityField indicator : indicators) {
            Collection<Integer> sourceIndicatorIds = linkedActivity.getSourceIndicatorIdsFor(indicator.getId());
            for (Integer sourceIndicatorId : sourceIndicatorIds) {
                String alias = "I" + sourceIndicatorId;
                queryModel.selectExpr(fieldExpression(sourceIndicatorId)).as(alias);
                aliasToIndicator.put(alias, indicator);
            }
        }

        // Add dimensions columns as needed
        // These are the columns we will use for grouping
        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        // if any of the indicators are "site count" indicators, then we need
        // to query the site id as well
        addSiteIdToQuery(activity, queryModel);

        // declare the filter
        queryModel.setFilter(composeFilter(formTree, permissionFilter));

        // Query the table 
        ColumnSet columnSet = executeQuery(queryModel);

        // Now add the results to the buckets
        aggregateTime.start();
        int[] siteIds = extractSiteIds(columnSet);
        DimensionCategory[][] categories = extractCategories(activity, columnSet);


        for (String sourceAlias : aliasToIndicator.keySet()) {
            ColumnView measureView = columnSet.getColumnView(sourceAlias);

            // A single source indicator may be mapped to multiple destination Indicators
            for (ActivityField destinationIndicator : aliasToIndicator.get(sourceAlias)) {
                DimensionCategory indicatorCategory = null;

                if (indicatorDimension.isPresent()) {
                    indicatorCategory = indicatorDimension.get().category(destinationIndicator);
                }
                for (int i = 0; i < columnSet.getNumRows(); i++) {

                    if (destinationIndicator.getAggregation() == IndicatorDTO.AGGREGATE_SITE_COUNT) {

                        Map<Dimension, DimensionCategory> key = bucketKey(i, categories, indicatorCategory);
                        Accumulator bucket = bucketForKey(key, destinationIndicator.getAggregation());
                        bucket.addSite(siteIds[i]);

                    } else {
                        double value = measureView.getDouble(i);
                        if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                            Map<Dimension, DimensionCategory> key = bucketKey(i, categories, indicatorCategory);

                            if (command.getValueType() == PivotSites.ValueType.INDICATOR) {
                                Accumulator bucket = bucketForKey(key, destinationIndicator.getAggregation());
                                bucket.addValue(value);
                            } else {
                                Accumulator bucket = bucketForKey(key, IndicatorDTO.AGGREGATE_SITE_COUNT);
                                bucket.addSite(siteIds[i]);
                            }
                        }
                    }
                }
            }
        }
        aggregateTime.stop();

    }

    private boolean isDeleted(Activity activity, LinkedActivity linkedActivity) throws SQLException {
        if(activity.isDeleted()) {
            return true;
        }
        if(activity.getId() != linkedActivity.getActivityId()) {
            // Should already be in session cache
            return session.getActivityLoader().load(linkedActivity.getActivityId()).isDeleted();
        }
        
        return false;
    }

    private String alias(ActivityField indicator) {
        return "I" + indicator.getId();
    }

    private ExprNode fieldExpression(int indicatorId) {
        return new SymbolExpr(CuidAdapter.indicatorField(indicatorId));
    }

    private void executeTargetValuesQuery(Integer databaseId) {

        // Check first if we are filtering on fields that are NOT present in the target
        // form. In this case, there can a priori be no target values that match
        // the filter on these values
        
        if(filter.isRestricted(DimensionType.AttributeGroup) ||
           filter.isRestricted(DimensionType.AdminLevel) ||
           filter.isRestricted(DimensionType.Site) || 
           filter.isRestricted(DimensionType.Location)) {
            
            // No results, exit now
            return;
        }
        
        ResourceId targetFormClassId = CuidAdapter.cuid(CuidAdapter.TARGET_FORM_CLASS_DOMAIN, databaseId);

        QueryModel queryModel = new QueryModel(targetFormClassId);
        queryModel.setFilter(composeTargetFilter());
        Collection<Activity> activities = databases.get(databaseId);

        // Add all indicators we're querying for
        for (Activity activity : activities) {
            for (ActivityField indicator : selectedIndicators(activity)) {
                queryModel.selectField(targetFieldExpr(indicator)).as(alias(indicator));
            }
        }

        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getTargetColumnQuery(targetFormClassId)) {
                queryModel.addColumn(columnModel);
            }
        }

        ColumnSet columnSet = executeQuery(queryModel);

        // Now add the results to the buckets
        aggregateTime.start();
        
        for (Activity activity : activities) {
            for (ActivityField indicator : selectedIndicators(activity)) {

                ColumnView measureView = columnSet.getColumnView(alias(indicator));
                DimensionCategory indicatorCategory = null;

                if (indicatorDimension.isPresent()) {
                    indicatorCategory = indicatorDimension.get().category(indicator);
                }
                for (int i = 0; i < columnSet.getNumRows(); i++) {
                    double value = measureView.getDouble(i);
                    if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                        Map<Dimension, DimensionCategory> key = new HashMap<>();
                        for (DimBinding dim : groupBy) {
                            DimensionCategory category = dim.extractTargetCategory(activity, columnSet, i);
                            if(category != null) {
                                key.put(dim.getModel(), category);
                            }
                        }
                        if(indicatorCategory != null) {
                            key.put(indicatorDimension.get().getModel(), indicatorCategory);
                        }
                        
                        Accumulator bucket = bucketForKey(key, indicator.getAggregation());
                        bucket.addValue(value);
                    }
                }
            }
        }
        aggregateTime.stop();
    }

    private ResourceId targetFieldExpr(ActivityField indicator) {
        return CuidAdapter.targetIndicatorField(indicator.getId());
    }


    private Map<Dimension, DimensionCategory> bucketKey(int rowIndex, DimensionCategory[][] categories, 
                                                        @Nullable DimensionCategory indicatorCategory) {

        Map<Dimension, DimensionCategory> key = new HashMap<>();

        // Only include indicator as dimension if we are pivoting on dimension
        if (indicatorCategory != null) {
            key.put(indicatorDimension.get().getModel(), indicatorCategory);
        }

        for (int j = 0; j < groupBy.size(); j++) {
            Dimension dimension = groupBy.get(j).getModel();
            DimensionCategory category = categories[j][rowIndex];
            if(category != null) {
                key.put(dimension, category);
            }
        }
        return key;
    }

    private Accumulator bucketForKey(Map<Dimension, DimensionCategory> key, int aggregation) {
        Accumulator bucket = buckets.get(key);
        if(bucket == null) {
            bucket = new Accumulator(key, aggregation);
            buckets.put(key, bucket);
        } else {
            bucket.maybeUpdateAggregationMethod(aggregation);
        }
        return bucket;
    }

    /**
     * Queries the count of distinct sites (not monthly reports) that match the filter
     */
    private void executeSiteCountQuery(Activity activity, 
                                       LinkedActivity linkedActivity, 
                                       Optional<ExprNode> permissionFilter) throws SQLException {
        
        // Check first that this activity hasn't been deleted
        if(isDeleted(activity, linkedActivity)) {
            return;
        }

        if(command.isPivotedBy(DimensionType.Indicator) ||
           command.getFilter().isRestricted(DimensionType.Indicator)) {
            
            // only count sites which have non-empty values for the given
            // indicators
            executeIndicatorValuesQuery(activity, linkedActivity, permissionFilter);
        
        } else if(activity.isMonthly() && 
                (command.isPivotedBy(DimensionType.Date) ||
                 command.getFilter().isEndDateRestricted())) {
            
            // if we are pivoting or filtering by date, then we need
            // to query the actual reporting periods and count distinct sites
                
            executeSiteCountQueryOnReportingPeriod(activity, linkedActivity, permissionFilter);
        
        } else {
            
            // Otherwise, we only need to query the sites add up the total rows
            executeSiteCountQueryOnSite(activity, linkedActivity, permissionFilter);
        }
    }

    private void executeSiteCountQueryOnSite(Activity activity, LinkedActivity linkedActivity, Optional<ExprNode> permissionFilter) {
        Preconditions.checkState(!indicatorDimension.isPresent());

        FormTree formTree = formTrees.get(linkedActivity.getSiteFormClassId());
        QueryModel queryModel = new QueryModel(linkedActivity.getSiteFormClassId());
        queryModel.setFilter(composeFilter(formTree, permissionFilter));

        // Add dimensions columns as needed
        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        // Query the table 
        ColumnSet columnSet = executeQuery(queryModel);

        aggregateTime.start();

        // Now add the counts to the buckets
        DimensionCategory[][] categories = extractCategories(activity, columnSet);

        for (int i = 0; i < columnSet.getNumRows(); i++) {

            Map<Dimension, DimensionCategory> key = bucketKey(i, categories, null);
            Accumulator bucket = bucketForKey(key, IndicatorDTO.AGGREGATE_SITE_COUNT);

            bucket.addCount(1);
        }
        aggregateTime.stop();
    }

    private void executeSiteCountQueryOnReportingPeriod(Activity activity, 
                                                        LinkedActivity linkedActivity, 
                                                        Optional<ExprNode> permissionFilter) {
        Preconditions.checkArgument(activity.isMonthly());

        // Query the linked activity
        FormTree formTree = formTrees.get(linkedActivity.getLeafFormClassId());
        QueryModel queryModel = new QueryModel(activity.getLeafFormClassId());
        
        queryModel.setFilter(composeFilter(formTree, permissionFilter));

        // Add dimensions columns as needed
        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        addSiteIdToQuery(activity, queryModel);

        // Query the table 
        ColumnSet columnSet = executeQuery(queryModel);

        aggregateTime.start();

        // Now add the counts to the buckets
        DimensionCategory[][] categories = extractCategories(activity, columnSet);
        int siteId[] = extractSiteIds(columnSet);

        for (int i = 0; i < columnSet.getNumRows(); i++) {

            Map<Dimension, DimensionCategory> key = bucketKey(i, categories, null);
            Accumulator bucket = bucketForKey(key, IndicatorDTO.AGGREGATE_SITE_COUNT);

            bucket.addSite(siteId[i]);
        }
        aggregateTime.stop();
    }
    
    private ColumnSet executeQuery(QueryModel queryModel) {
        queryTime.start();
        ColumnSetBuilder builder = new ColumnSetBuilder(session);
        ColumnSet columnSet = builder.build(queryModel);
        queryTime.stop();
        return columnSet;
    }

    private void addSiteIdToQuery(Activity activity, QueryModel queryModel) {
        if(activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            queryModel.selectResourceId().as(SITE_ID_KEY);
        } else {
            queryModel.selectField(CuidAdapter.field(activity.getLeafFormClassId(), CuidAdapter.SITE_FIELD)).as(SITE_ID_KEY);
        }
    }

    private int[] extractSiteIds(ColumnSet columnSet) {
        ColumnView columnView = columnSet.getColumnView(SITE_ID_KEY);
        int[] ids = new int[columnView.numRows()];

        for (int i = 0; i < columnView.numRows(); i++) {
            String resourceId = columnView.getString(i);
            if(resourceId != null) {
                ids[i] = CuidAdapter.getLegacyIdFromCuid(resourceId);
            }
        }
        return ids;
    }

    private List<Bucket> createBuckets() {
        List<Bucket> list = Lists.newArrayList();
        for (Accumulator accumulator : buckets.values()) {
            list.add(accumulator.createBucket());
        }
        return list;
    }

    private ExprNode composeFilter(FormTree formTree, Optional<ExprNode> permissionFilter) {
        List<ExprNode> conditions = Lists.newArrayList();
        conditions.addAll(filterExpr(ColumnModel.ID_SYMBOL, CuidAdapter.SITE_DOMAIN, DimensionType.Site));
        conditions.addAll(filterExpr("partner", CuidAdapter.PARTNER_DOMAIN, DimensionType.Partner));
        conditions.addAll(filterExpr("project", CuidAdapter.PROJECT_DOMAIN, DimensionType.Project));
        conditions.addAll(filterExpr("location", CuidAdapter.LOCATION_DOMAIN, DimensionType.Location));
        conditions.addAll(adminFilter(formTree));
        conditions.addAll(attributeFilters());
        conditions.addAll(dateFilter("date1", filter.getStartDateRange()));
        conditions.addAll(dateFilter("date2", filter.getEndDateRange()));
        
        if(permissionFilter.isPresent()) {
            conditions.add(permissionFilter.get());
        }

        if(conditions.size() > 0) {
            ExprNode filterExpr = Exprs.allTrue(conditions);
            LOGGER.info("Filter: " + filterExpr);

            return filterExpr;

        } else {
            return null;
        }
    }

    private ExprNode composeTargetFilter() {
        List<ExprNode> conditions = Lists.newArrayList();
        conditions.addAll(filterExpr("partner", CuidAdapter.PARTNER_DOMAIN, DimensionType.Partner));
        conditions.addAll(filterExpr("project", CuidAdapter.PROJECT_DOMAIN, DimensionType.Project));

        conditions.addAll(dateFilter("fromDate", filter.getStartDateRange()));
        conditions.addAll(dateFilter("toDate", filter.getEndDateRange()));
        
        if(conditions.isEmpty()) {
            return null;
        }
        
        ExprNode filterExpr = Exprs.allTrue(conditions);
        LOGGER.info("Filter: " + filterExpr);

        return filterExpr;
    }
    

    private Set<ExprNode> adminFilter(FormTree formTree) {
        if (this.filter.isRestricted(DimensionType.AdminLevel)) {

            List<ExprNode> conditions = Lists.newArrayList();

            // we don't know which adminlevel this belongs to so we have construct a giant OR statement
            List<ExprNode> adminIdExprs = findAdminIdExprs(formTree);

            for(ExprNode adminIdExpr : adminIdExprs) {
                for (Integer adminEntityId : this.filter.getRestrictions(DimensionType.AdminLevel)) {
                    conditions.add(Exprs.equals(adminIdExpr, idConstant(CuidAdapter.entity(adminEntityId))));
                }
            }

            return singleton(anyTrue(conditions));

        } else {
            return emptySet();
        }
    }

    private List<ExprNode> findAdminIdExprs(FormTree formTree) {
        List<ExprNode> expressions = Lists.newArrayList();
        for (FormClass formClass : formTree.getFormClasses()) {
            if(formClass.getId().getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN) {
                expressions.add(new CompoundExpr(formClass.getId(), ColumnModel.ID_SYMBOL));
            }
        }
        return expressions;
    }

    private List<ExprNode> attributeFilters() {

        List<ExprNode> conditions = Lists.newArrayList();

        for (String field : attributeFilters.keySet()) {
            List<ExprNode> valueConditions = Lists.newArrayList();
            for (String value : attributeFilters.get(field)) {
                valueConditions.add(new CompoundExpr(new SymbolExpr(field), new SymbolExpr(value)));
            }
            conditions.add(Exprs.anyTrue(valueConditions));
        }
        return conditions;
    }

    private List<ExprNode> filterExpr(String fieldName, char domain, DimensionType type) {
        Set<Integer> ids = this.filter.getRestrictions(type);
        if(ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<ExprNode> conditions = Lists.newArrayList();

            for (Integer id : ids) {
                conditions.add(Exprs.equals(symbol(fieldName), idConstant(CuidAdapter.cuid(domain, id))));
            }

            return singletonList(Exprs.anyTrue(conditions));
        }
    }


    private Collection<FunctionCallNode> dateFilter(String dateField, DateRange range) {
        if(range.isRestricted()) {

            SymbolExpr dateExpr = new SymbolExpr(dateField);
            ConstantExpr minDate = new ConstantExpr(new LocalDate(range.getMinDate()));
            ConstantExpr maxDate = new ConstantExpr(new LocalDate(range.getMaxDate()));

            return singleton(
                    new FunctionCallNode(AndFunction.INSTANCE,
                            new FunctionCallNode(GreaterOrEqualFunction.INSTANCE, dateExpr, minDate),
                            new FunctionCallNode(LessOrEqualFunction.INSTANCE, dateExpr, maxDate)));

        } else {
            return Collections.emptyList();
        }
    }

    private DimensionCategory[][] extractCategories(Activity activity, ColumnSet columnSet) {
        DimensionCategory[][] array = new DimensionCategory[groupBy.size()][];

        for (int i = 0; i < groupBy.size(); i++) {
            array[i] = groupBy.get(i).extractCategories(activity, columnSet);
        }
        return array;
    }
}
