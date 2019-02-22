package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.columns.BlockResolver;
import org.activityinfo.store.hrd.columns.RecordIdBlock;
import org.activityinfo.store.hrd.columns.TombstoneIndex;
import org.activityinfo.store.hrd.entity.FormEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        // Before activating column block storage, we need to ensure that
        // all records are numbered.

        Query query = new Query("FormRecord")
                .setAncestor(FormEntity.key(formId).getRaw())
                .setKeysOnly();

        DatastoreInput datastoreInput = new DatastoreInput(query, 1);
        BatchingInput<Entity> batchingInput = new BatchingInput<>(datastoreInput, 10);
        BlockBuilder builder = new BlockBuilder(formId);
        MapSpecification<List<Entity>, Void, Void> spec = new MapSpecification.Builder<List<Entity>, Void, Void>(batchingInput, builder)
                .setJobName("Construct column blocks")
                .build();

        MapJob<List<Entity>, Void, Void> mapJob = new MapJob<>(spec, MigrationServlet.getSettings());

        ActivateColumnStorage activateJob = new ActivateColumnStorage(formId);

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
