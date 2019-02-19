package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.columns.BlockResolver;
import org.activityinfo.store.hrd.columns.RecordIdBlock;
import org.activityinfo.store.hrd.columns.TombstoneIndex;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockJob extends Job0<Void> {

    private static final Logger LOGGER = Logger.getLogger(BlockJob.class.getName());

    private static final int BATCH_SIZE = 5;

    private ResourceId formId;

    public BlockJob(String formId) {
        this.formId = ResourceId.valueOf(formId);
    }

    @Override
    public Value<Void> run() throws Exception {

        List<String> toNumber = new ArrayList<>();

        try(Closeable o = ObjectifyService.begin()) {

            // First read in the array of blocks that DO have record numbers
            // Based on the ID block
            FormEntity formEntity = Hrd.ofy().load().key(FormEntity.key(formId)).safe();
            Set<String> numberedRecords = queryAssignedRecordIds(formEntity);

            // Now query all the record keys
            QueryResultIterable<Key<FormRecordEntity>> keys = Hrd.ofy().load()
                    .type(FormRecordEntity.class)
                    .ancestor(formEntity)
                    .chunk(5000)
                    .keys()
                    .iterable();

            for (Key<FormRecordEntity> key : keys) {
                if(!numberedRecords.contains(key.getName())) {
                    if(toNumber.size() > 100) {
                        break;
                    }
                    toNumber.add(key.getName());
                }
            }

            LOGGER.info("Found " + toNumber.size() + " records without number");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        ActivateColumnStorage activateJob = new ActivateColumnStorage(formId);

        if(toNumber.isEmpty()) {
            LOGGER.info("All records are numbered, activating column storage");
            return futureCall(activateJob);
        }

        RecordBatchInput input = new RecordBatchInput(formId.asString(), toNumber, BATCH_SIZE);
        BlockBuilder builder = new BlockBuilder();
        MapSpecification<RecordBatch, Void, Void> spec = new MapSpecification.Builder<RecordBatch, Void, Void>(input, builder)
                .setJobName("Migrate MapReduce entities")
                .build();

        MapJob<RecordBatch, Void, Void> mapJob = new MapJob<>(spec, MigrationServlet.getSettings());

        return futureCall(activateJob, waitFor(futureCall(mapJob)));
    }

    private Set<String> queryAssignedRecordIds(FormEntity formEntity) {
        BlockResolver resolver = new BlockResolver(null, formEntity);
        resolver.fetchRecordIds();
        resolver.fetchTombstones();
        resolver.load();

        TombstoneIndex tombstones = new TombstoneIndex(formEntity, resolver.getTombstoneBlocks());

        RecordIdBlock block = new RecordIdBlock();
        ColumnView columnView = block.buildView(formEntity, tombstones, resolver.getBlocks(RecordIdBlock.BLOCK_NAME));

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < columnView.numRows(); i++) {
            ids.add(columnView.getString(i));
        }
        return ids;
    }
}
