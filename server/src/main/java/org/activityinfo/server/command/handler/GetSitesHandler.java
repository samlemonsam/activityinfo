package org.activityinfo.server.command.handler;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.exception.CommandException;
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
import org.activityinfo.store.mysql.metadata.LinkedActivity;
import org.activityinfo.store.query.impl.*;
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

    private MySqlCatalog catalog;
    private ColumnSetBuilder builder;
    private BatchingFormTreeBuilder batchFormTreeBuilder;
    private FormScanBatch batch;

    private Map<ResourceId,FormTree> formTreeMap;
    private Map<ResourceId,QueryModel> queryMap = new HashMap<>();
    private Map<ResourceId,List<FieldBinding>> fieldBindingMap = new HashMap<>();
    private List<Runnable> queryResultHandlers = new ArrayList<>();

    private Map<Integer,Activity> activities;
    private List<SiteDTO> siteList = Lists.newLinkedList();

    private final Stopwatch metadataTime = Stopwatch.createUnstarted();
    private final Stopwatch treeTime = Stopwatch.createUnstarted();
    private final Stopwatch queryBuildTime = Stopwatch.createUnstarted();
    private final Stopwatch queryExecTime = Stopwatch.createUnstarted();
    private final Stopwatch aggregateTime = Stopwatch.createUnstarted();

    @Override
    public SiteResult execute(GetSites command, User user) {

        if (command.isLegacyFetch()) {
            return dispatcher.execute(new OldGetSites(command));
        }
        LOGGER.info("Entering execute()");
        aggregateTime.start();
        initialiseHandler(user);
        fetchActivityMetadata(command.getFilter());
        queryFormTrees(command);
        buildQueries(command);
        batchQueries();
        executeBatch();
        aggregateTime.stop();

        printTimes();
        LOGGER.info("Exiting execute()");
        return new SiteResult(siteList);
    }

    private void printTimes() {
        LOGGER.info("GetSites timings: {" + "Metadata Fetch: " + metadataTime.toString() + "; " +
                "Form Tree Fetch: " + treeTime.toString() + "; " +
                "Query Build: " + queryBuildTime.toString() + "; " +
                "Query Execution : " + queryExecTime.toString() + "; " +
                "Aggregate Time: " + aggregateTime.toString()
        + "}");
    }

    private void initialiseHandler(User user) {
        catalog = catalogProvider.get();
        if (catalog != null) {
            builder = new ColumnSetBuilder(catalog, new AppEngineFormScanCache(), new FormSupervisorAdapter(catalog, user.getId()));
            batchFormTreeBuilder = new BatchingFormTreeBuilder(catalog);
            batch = builder.createNewBatch();
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

    private void queryFormTrees(GetSites command) {
        treeTime.start();
        Set<ResourceId> formIds = new HashSet<>();
        for (Activity activity : activities.values()) {
            formIds.add(activity.getLeafFormClassId());
            if (command.isFetchLinks()) {
                for (LinkedActivity linkedActivity : activity.getLinkedActivities()) {
                    formIds.add(linkedActivity.getLeafFormClassId());
                }
            }
        }
        formTreeMap = batchFormTreeBuilder.queryTrees(formIds);
        treeTime.stop();
    }

    private void buildQueries(GetSites command) {
        queryBuildTime.start();
        for (Activity activity : activities.values()) {
            FormTree activityFormTree = formTreeMap.get(CuidAdapter.activityFormClass(activity.getId()));
            QueryModel query = buildQuery(activityFormTree, command);
            query.setFilter(determineQueryFilter(command.getFilter(), activityFormTree));
            queryMap.put(CuidAdapter.activityFormClass(activity.getId()), query);
        }
        queryBuildTime.stop();
    }

    private void batchQueries() {
        for (final Map.Entry<ResourceId,QueryModel> queryEntry : queryMap.entrySet()) {
            enqueueQuery(queryEntry.getValue(), new Function<ColumnSet, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ColumnSet columnSet) {
                    List<FieldBinding> fieldBindings = fieldBindingMap.get(queryEntry.getKey());
                    SiteDTO[] sites = initialiseSites(columnSet.getNumRows());

                    for (FieldBinding binding : fieldBindings) {
                        binding.extractFieldData(sites, columnSet);
                    }

                    siteList.addAll(Lists.newArrayList(sites));
                    return null;
                }
            });
        }
    }

    private SiteDTO[] initialiseSites(int length) {
        SiteDTO[] array = new SiteDTO[length];
        for (int i=0; i<array.length; i++) {
            array[i] = new SiteDTO();
        }
        return array;
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
            batch.execute();
        } catch (Exception excp) {
            throw new RuntimeException("Failed to execute query batch", excp);
        }
        for (Runnable handler : queryResultHandlers) {
            handler.run();
        }
        queryExecTime.stop();
    }

    private QueryModel buildQuery(FormTree formTree, GetSites command) {
        return buildQuery(formTree, formTree.getRootFormClass(), command);
    }

    private ExprNode determineQueryFilter(Filter commandFilter, FormTree formTree) {
        QueryFilter queryFilter = new QueryFilter(commandFilter, HashMultimap.<String, String>create());
        return queryFilter.composeFilter(formTree);
    }

    private QueryModel buildQuery(FormTree formTree, FormClass activityForm, GetSites command) {
        QueryModel query = new QueryModel(activityForm.getId());
        fieldBindingMap.put(activityForm.getId(), Lists.<FieldBinding>newLinkedList());

        addBinding(new SiteDimBinding(), query, formTree);
        addBinding(new ActivityIdFieldBinding(), query, formTree);
        addBinding(new ProjectDimBinding(), query, formTree);
        if (command.isFetchDates()) {
            addBinding(new StartEndDateFieldBinding(), query, formTree);
        }
        if (command.isFetchPartner()) {
            addBinding(new PartnerDimBinding(), query, formTree);
        }
        if (command.isFetchLocation()) {
            query = buildLocationQuery(query, formTree, activityForm);
        }
        if (command.isFetchAttributes()) {
            query = buildAttributeQuery(query, formTree, activityForm);
        }
        if (command.fetchAnyIndicators()) {
            query = buildIndicatorQuery(query, formTree, command, activityForm);
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
            ResourceId locationReferenceId = getReferenceId(locationField.getType());
            FormClass locationForm = formTree.getFormClass(locationReferenceId);
            addBinding(new LocationFieldBinding(locationForm), query, formTree);
            return buildLocationQuery(query, formTree, locationForm);
        } else {
            // country form, get country instance from ActivityLoader
            CountryInstance country = getCountryInstance(form.getId());
            if (country != null) {
                addBinding(new CountryFieldBinding(country), query, formTree);
            }
            return query;
        }
    }

    private CountryInstance getCountryInstance(ResourceId locationFormId) {
        try {
            Activity activity = activities.get(CuidAdapter.getLegacyIdFromCuid(locationFormId));
            CountryInstance country = catalog.getActivityLoader().loadCountryInstance(activity.getLocationTypeId());
            return country;
        } catch (SQLException excp) {
            return null;
        }
    }

    private QueryModel addGeoField(QueryModel query, FormTree formTree, FormClass form) {
        FormField geoField = getField(form, CuidAdapter.field(form.getId(), CuidAdapter.GEOMETRY_FIELD));
        if (geoField != null) {
            query = buildGeoLocationQuery(query, formTree, geoField);
        }
        return query;
    }

    private QueryModel addAdminField(QueryModel query, FormTree formTree, FormClass form, int fieldIndex) {
        FormField adminField = getField(form, CuidAdapter.field(form.getId(), fieldIndex));
        if (adminField != null) {
            ResourceId adminReferenceId = getReferenceId(adminField.getType());
            return buildLocationQuery(query, formTree, formTree.getFormClass(adminReferenceId));
        }
        return query;
    }

    private QueryModel buildGeoLocationQuery(QueryModel query, FormTree formTree, FormField geoField) {
        if (geoField.getType() instanceof GeoPointType) {
            addBinding(new GeoPointFieldBinding(geoField), query, formTree);
        } else if (geoField.getType() instanceof GeoAreaType) {
            // TODO: GeoArea Binding
            //addBinding(new GeoAreaFieldBinding(geoField), query, formTree);
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

    private QueryModel buildAttributeQuery(QueryModel query, FormTree formTree, FormClass activityForm) {
        for (FormField field : activityForm.getFields()) {
            if (field.getType() instanceof EnumType) {
                addBinding(new AttributeFieldBinding(field), query, formTree);
            }
        }
        return query;
    }

    private QueryModel buildIndicatorQuery(QueryModel query, FormTree formTree, GetSites command, FormClass activityForm) {
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
