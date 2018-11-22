package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.google.common.base.Stopwatch;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.hrd.op.ColumnBlockUpdater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class BlockBuilder extends MapOnlyMapper<RecordBatch, Void> {

    private static final Logger LOGGER = Logger.getLogger(BlockBuilder.class.getName());

    private transient Closeable objectifySession;

    @Override
    public void beginSlice() {
        super.beginSlice();
        objectifySession = ObjectifyService.begin();
    }

    @Override
    public void endSlice() {
        super.endSlice();
        objectifySession.close();
    }

    @Override
    public void map(RecordBatch batch) {

        ResourceId formId = ResourceId.valueOf(batch.getFormId());

        Hrd.ofy().transact(new VoidWork() {
            @Override
            public void vrun() {

                Stopwatch stopwatch = Stopwatch.createStarted();

                FormEntity rootEntity = Hrd.ofy().load().key(FormEntity.key(formId)).now();
                FormClass formSchema = Hrd.ofy().load().key(FormSchemaEntity.key(formId)).now().readFormClass();

                // We have to update the version number otherwise caching will break...
                rootEntity.setVersion(rootEntity.getVersion() + 1);

                ColumnBlockUpdater blockUpdater = new ColumnBlockUpdater(rootEntity, formSchema, Hrd.ofy().getTransaction());

                // Fetch the records to update
                List<Key<FormRecordEntity>> recordKeys = new ArrayList<>();
                for (String recordId : batch.getRecords()) {
                    recordKeys.add(FormRecordEntity.key(formId, ResourceId.valueOf(recordId)));
                }

                Collection<FormRecordEntity> records = Hrd.ofy().load().keys(recordKeys).values();

                List<FormRecordEntity> updatedRecords = new ArrayList<>();
                for (FormRecordEntity record : records) {
                    if(record.getRecordNumber() == 0) {
                        updateBlocks(formSchema, rootEntity, record, blockUpdater);
                        updatedRecords.add(record);
                    }
                }

                // Commit the changes
                if (!updatedRecords.isEmpty()) {

                    LOGGER.info("Updating root entity " + Key.create(rootEntity).getRaw());

                    List<Object> toSave = new ArrayList<>();
                    toSave.add(rootEntity);
                    toSave.addAll(updatedRecords);
                    toSave.addAll(blockUpdater.getUpdatedBlocks());
                    Hrd.ofy().save().entities(toSave).now();

                    LOGGER.info(String.format("Updated batch of %s (%d entities) in %s",
                            updatedRecords.size(), toSave.size(), stopwatch));
                }
            }
        });
    }


    private void updateBlocks(FormClass formSchema, FormEntity rootEntity, FormRecordEntity record, ColumnBlockUpdater blockUpdater) {
        int newRecordCount = rootEntity.getNumberedRecordCount() + 1;
        int newRecordNumber = newRecordCount;

        record.setRecordNumber(newRecordNumber);
        rootEntity.setNumberedRecordCount(newRecordCount);

        blockUpdater.updateId(newRecordNumber, record.getRecordId().asString());

        if(record.getParentRecordId() != null) {
            blockUpdater.updateParentId(newRecordNumber, record.getParentRecordId());
        }

        FormRecord formRecord = record.toFormRecord(formSchema);
        TypedFormRecord typedRecord = TypedFormRecord.toTypedFormRecord(formSchema, formRecord);

        blockUpdater.updateFields(newRecordNumber, typedRecord.getFieldValueMap());
    }
}
