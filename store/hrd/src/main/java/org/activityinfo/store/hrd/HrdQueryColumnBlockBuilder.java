package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.googlecode.objectify.LoadResult;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.hrd.columns.BlockDescriptor;
import org.activityinfo.store.hrd.columns.BlockFactory;
import org.activityinfo.store.hrd.columns.BlockManager;
import org.activityinfo.store.hrd.columns.RecordIdBlock;
import org.activityinfo.store.hrd.entity.FormColumnStorage;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.spi.ColumnQueryBuilderV2;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.PendingSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HrdQueryColumnBlockBuilder implements ColumnQueryBuilderV2 {


    private final FormEntity formEntity;
    private Multimap<ResourceId, PendingSlot<ColumnView>> fieldTargets = HashMultimap.create();
    private List<PendingSlot<ColumnView>> idTargets = new ArrayList<>();
    private List<PendingSlot<Integer>> rowCountTargets = new ArrayList<>();

    public HrdQueryColumnBlockBuilder(FormEntity formEntity) {
        this.formEntity = formEntity;
    }

    @Override
    public void addField(ResourceId fieldId, PendingSlot<ColumnView> target) {
        fieldTargets.put(fieldId, target);
    }

    @Override
    public void addEnumItem(ResourceId fieldId, ResourceId enumId, PendingSlot<ColumnView> target) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addFieldComponent(ResourceId fieldId, ResourceId enumId, PendingSlot<ColumnView> target) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addRecordId(PendingSlot<ColumnView> target) {
        idTargets.add(target);
    }

    @Override
    public void addRowCount(PendingSlot<Integer> rowCount) {
        rowCountTargets.add(rowCount);
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void execute() {

        LoadResult<FormColumnStorage> columnMap = Hrd.ofy().load().key(FormColumnStorage.key(formEntity));
        LoadResult<FormSchemaEntity> schema = Hrd.ofy().load().key(FormSchemaEntity.key(formEntity.getResourceId()));

        // Provide row counts
        for (PendingSlot<Integer> rowCountTarget : rowCountTargets) {
            rowCountTarget.set(columnMap.safe().getRecordCount() - columnMap.safe().getDeletedCount());
        }

        // Start queries for required blocks.
        // This will run asynchronously and in parallel against the datastore.

        Map<ResourceId, QueryResultIterator<Entity>> queries = new HashMap<>();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        if(!idTargets.isEmpty()) {
            queries.put(RecordIdBlock.FIELD_ID, queryBlocks(datastore, RecordIdBlock.FIELD_ID));
        }

        for (ResourceId fieldId : fieldTargets.keySet()) {
            queries.put(fieldId, queryBlocks(datastore, fieldId));
        }

        // Now construct column views from blocks

        FormClass formSchema = schema.now().readFormClass();

        if(!idTargets.isEmpty()) {
            BlockManager blockManager = new RecordIdBlock();
            ColumnView columnView = blockManager.buildView(columnMap.safe(), queries.get(RecordIdBlock.FIELD_ID));
            for (PendingSlot<ColumnView> idTarget : idTargets) {
                idTarget.set(columnView);
            }
        }

        for (ResourceId fieldId : fieldTargets.keySet()) {
            FormField field = formSchema.getField(fieldId);
            BlockManager blockManager = BlockFactory.get(field.getType());

            ColumnView columnView = blockManager.buildView(columnMap.safe(), queries.get(fieldId));
            for (PendingSlot<ColumnView> fieldTarget : fieldTargets.get(fieldId)) {
                fieldTarget.set(columnView);
            }
        }



    }

    private QueryResultIterator<Entity> queryBlocks(DatastoreService datastore, ResourceId fieldId) {
        com.google.appengine.api.datastore.Key columnKey = BlockDescriptor.columnKey(formEntity.getResourceId(), fieldId);
        Query query = new Query(BlockDescriptor.BLOCK_KIND, columnKey);
        PreparedQuery preparedQuery = datastore.prepare(Hrd.ofy().getTransaction(), query);

        return preparedQuery.asQueryResultIterator(
                FetchOptions.Builder
                        .withChunkSize(1000)
                        .prefetchSize(1000));
    }
}
