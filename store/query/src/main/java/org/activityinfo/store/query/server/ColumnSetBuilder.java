package org.activityinfo.store.query.server;

import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.*;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ColumnSetBuilder {

    public static final Logger LOGGER = Logger.getLogger(ColumnSetBuilder.class.getName());

    private final FormCatalog catalog;
    private final FormTreeBuilder formTreeBuilder;
    private final FormSupervisor supervisor;
    private final FormScanCache cache;

    public ColumnSetBuilder(FormCatalog catalog, FormScanCache cache, FormSupervisor supervisor) {
        this.catalog = catalog;
        this.formTreeBuilder = new FormTreeBuilder(catalog);
        this.cache = cache;
        this.supervisor = supervisor;
    }

    public ColumnSetBuilder(FormCatalog catalog, FormSupervisor supervisor) {
        this(catalog, new AppEngineFormScanCache(), supervisor);
    }

    public FormScanBatch createNewBatch() {
        return new FormScanBatch(catalog, supervisor);
    }

    public ColumnSet build(QueryModel queryModel) {

        // We want to make at most one pass over every collection we need to scan,
        // so first queue up all necessary work before executing
        FormScanBatch batch = createNewBatch();

        // Enqueue the columns we need
        Slot<ColumnSet> columnSet = enqueue(queryModel, batch);

        // Now execute the batch
        try {
            execute(batch);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query batch", e);
        }

        return columnSet.get();
    }

    public void execute(FormScanBatch batch) {

        List<Future<Integer>> pendingCachePuts = new ArrayList<>();

        // Before hitting the database, retrieve what we can from the cache
        resolveFromCache(batch);

        // Now hit the database for anything remaining...
        for(FormScan scan : batch.getScans()) {

            executeScan(scan);

            // Send separate (async) cache put requests after each collection to avoid
            // having to serialize everything at once and risking OutOfMemoryErrors
            pendingCachePuts.addAll(cache(scan));
        }

        waitForCachingToFinish(pendingCachePuts);

    }

    private void resolveFromCache(FormScanBatch batch) {

        Set<String> toFetch = new HashSet<>();

        // Collect the keys we need from all enqueued tables
        for (FormScan formScan : batch.getScans()) {
            toFetch.addAll(formScan.getCacheKeys());
        }

        if (!toFetch.isEmpty()) {
            Map<String, Object> cached = cache.getAll(toFetch);

            // Now populate the individual collection scans with what we got back from memcache
            // with a little luck nothing will be left to query directly from the database
            for (FormScan formScan : batch.getScans()) {
                formScan.updateFromCache(cached);
            }
        }
    }

    private void executeScan(FormScan scan) {
        Optional<FormStorage> form = catalog.getForm(scan.getFormId());
        if(!form.isPresent()) {
            throw new IllegalStateException("No storage for form " + scan.getFormId());
        }

        ColumnQueryBuilder queryBuilder = form.get().newColumnQuery();

        scan.prepare(queryBuilder);

        // Run the query
        Stopwatch stopwatch = Stopwatch.createStarted();
        queryBuilder.execute();

        LOGGER.info("Form scan of " + scan.getFormId() + " completed in " + stopwatch);
    }



    public List<Future<Integer>> cache(FormScan scan) {
        try {
            Map<String, Object> toPut = scan.getValuesToCache();
            if(!toPut.isEmpty()) {
                Future<Integer> future = cache.enqueuePut(toPut);
                if(!future.isDone()) {
                    return Collections.singletonList(future);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to start memcache put for " + scan);
        }

        return Collections.emptyList();
    }

    /**
     * Wait for caching to finish, if there is time left in this request.
     */
    public void waitForCachingToFinish(List<Future<Integer>> pendingCachePuts) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        int columnCount = 0;
        for (Future<Integer> future : pendingCachePuts) {
            if (!future.isDone()) {
                long remainingMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
                if (remainingMillis > 100) {
                    try {
                        Integer cachedCount = future.get(remainingMillis - 50, TimeUnit.MILLISECONDS);
                        columnCount += cachedCount;

                    } catch (InterruptedException | TimeoutException e) {
                        LOGGER.warning("Ran out of time while waiting for caching of results to complete.");
                        return;

                    } catch (ExecutionException e) {
                        LOGGER.log(Level.WARNING, "Exception caching results of query", e);
                    }
                }
            }
        }

        LOGGER.info("Waited " + stopwatch + " for " + columnCount + " columns to finish caching.");
    }


    public Slot<ColumnSet> enqueue(QueryModel table, FormScanBatch batch) {
        ResourceId formId = table.getRowSources().get(0).getRootFormId();
        FormTree tree = formTreeBuilder.queryTree(formId);

        return enqueue(tree, table, batch);
    }

    public static Slot<ColumnSet> enqueue(FormTree tree, QueryModel model, FormScanBatch batch) {
        FormClass formClass = tree.getRootFormClass();
        Preconditions.checkNotNull(formClass);

        QueryEvaluator evaluator = new QueryEvaluator(FilterLevel.PERMISSIONS, tree, batch);
        return evaluator.evaluate(model);

    }

}
