package org.activityinfo.server.command.handler.pivot;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.model.AdminDimension;
import org.activityinfo.legacy.shared.reports.model.AttributeGroupDimension;
import org.activityinfo.legacy.shared.reports.model.DateDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.command.handler.IndicatorOracle;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

import java.util.*;
import java.util.logging.Logger;

/**
 * Executes a legacy PivotSites query against the new API
 */
public class PivotAdapter {
    
    private static final Logger LOGGER = Logger.getLogger(PivotAdapter.class.getName());

    private final IndicatorOracle indicatorOracle;
    private final CollectionCatalog catalog;
    private final PivotSites command;
    private final Filter filter;

    /**
     * Maps indicator ids to form class ids
     */
    private final Map<Integer, ResourceId> indicatorMap;

    private final Map<ResourceId, FormTree> formTrees;
    
    private final List<DimBinding> dimensions;
    private Optional<IndicatorDimBinding> indicatorDimension = Optional.absent();

    private final Map<Object, Bucket> buckets = Maps.newHashMap();

    private final Stopwatch queryTime = Stopwatch.createUnstarted();
    private final Stopwatch aggregateTime = Stopwatch.createUnstarted();
    

    public PivotAdapter(IndicatorOracle indicatorOracle, CollectionCatalog catalog, PivotSites command) {
        this.indicatorOracle = indicatorOracle;
        this.catalog = catalog;
        this.command = command;
        this.filter = command.getFilter();

        // Mapping from indicator id -> activityId
        Set<Integer> indicatorIds = filter.getRestrictions(DimensionType.Indicator);
        indicatorMap = indicatorOracle.indicatorToForm(indicatorIds);
        
        // Query form trees: needed to determine attribute mapping
        formTrees = queryFormTrees(indicatorMap.values());

        // Order the dimensions
        dimensions = new ArrayList<>();
        for (Dimension dimension : command.getDimensions()) {
            if(dimension.getType() == DimensionType.Indicator) {
                indicatorDimension = Optional.of(new IndicatorDimBinding());
            } else {
                dimensions.add(buildAccessor(dimension));
            }
        }
    }

    private Map<ResourceId, FormTree> queryFormTrees(Collection<ResourceId> formClassIds) {
        Map<ResourceId, FormTree> map = new HashMap<>();
        FormTreeBuilder builder = new FormTreeBuilder(catalog);

        for (ResourceId formClassId : formClassIds) {
            map.put(formClassId, builder.queryTree(formClassId));
        }
        return map;
    }

    private DimBinding buildAccessor(Dimension dimension) {
        switch (dimension.getType()) {
            case Partner:
                return new PartnerDimBinding();
            case Activity:
                return new ActivityDimBinding();
            case Date:
                return new DateDimBinding((DateDimension) dimension);
            case AttributeGroup:
                return new AttributeDimBinding((AttributeGroupDimension) dimension, formTrees.values());
            case AdminLevel:
                return new AdminDimBinding((AdminDimension) dimension);
            case Site:
            case ActivityCategory:
            case Database:
            case Status:
            case Attribute:
                break;
            case Project:
                break;
            case Location:
                break;
            case Target:
                break;
        }
        throw new UnsupportedOperationException("Unsupported dimension " + dimension);
    }


    public PivotSites.PivotResult execute() {
        for (FormTree formTree : formTrees.values()) {
            queryForm(formTree);
        }
        
        LOGGER.info("Query time: " + queryTime + ", aggregate: " + aggregateTime);
        
        return new PivotSites.PivotResult(Lists.newArrayList(buckets.values()));
    }

    private void queryForm(FormTree formTree) {
        ResourceId formClassId = formTree.getRootFormClass().getId();
        int activityId = CuidAdapter.getLegacyIdFromCuid(formClassId);
        QueryModel queryModel = new QueryModel(formTree.getRootFormClass().getId());

        // Add Indicators
        Set<Integer> indicatorIds = selectedIndicatorsInForm(formClassId);
        for (Integer indicatorId : indicatorIds) {
            queryModel.selectField(CuidAdapter.indicatorField(indicatorId)).as("I" + indicatorId);
        }

        // Add dimensions columns as needed
        for (DimBinding dimension : dimensions) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        // Query the table 
        queryTime.start();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, ColumnCache.NULL);
        ColumnSet columnSet = builder.build(queryModel);
        
        queryTime.stop();
        
        aggregateTime.start();
        
        // Now add the results to the buckets
        DimensionCategory[][] categories = extractCategories(formTree, columnSet);

        for (Integer indicatorId : indicatorIds) {
            ColumnView measureView = columnSet.getColumnView("I" + indicatorId);
            DimensionCategory indicatorCategory = null;

            if (indicatorDimension.isPresent()) {
                indicatorCategory = indicatorDimension.get().category(formTree, indicatorId);
            }

            for (int i = 0; i < columnSet.getNumRows(); i++) {

                double value = measureView.getDouble(i);
                if (!Double.isNaN(value)) {

                    Bucket bucket = new Bucket();
                    bucket.setCount(1);
                    bucket.setSum(value);

                    if (indicatorDimension.isPresent()) {
                        bucket.setCategory(indicatorDimension.get().getModel(), indicatorCategory);
                    }

                    for (int j = 0; j < dimensions.size(); j++) {
                        bucket.setCategory(dimensions.get(j).getModel(), categories[j][i]);
                    }

                    addBucket(bucket);
                }
            }
        }
        aggregateTime.stop();
    }

    
    private DimensionCategory[][] extractCategories(FormTree formTree, ColumnSet columnSet) {
        DimensionCategory[][] array = new DimensionCategory[dimensions.size()][];

        for (int i = 0; i < dimensions.size(); i++) {
            array[i] = dimensions.get(i).extractCategories(formTree, columnSet);
        }
        return array;
    }


    public void addBucket(Bucket bucket) {
        Bucket existing = buckets.get(bucket.getKey());
        if (existing == null) {
            buckets.put(bucket.getKey(), bucket);
        } else {
            existing.add(bucket);
        }
    }


    private Set<Integer> selectedIndicatorsInForm(ResourceId formClassId) {
        Set<Integer> indicatorIds = new HashSet<>();
        for (Map.Entry<Integer, ResourceId> entry : indicatorMap.entrySet()) {
            if(entry.getValue().equals(formClassId)) {
                indicatorIds.add(entry.getKey());
            }
        }
        return indicatorIds;
    }
}
