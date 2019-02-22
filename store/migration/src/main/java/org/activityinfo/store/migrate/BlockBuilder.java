package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.util.Closeable;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.TypedFormRecord;
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
import java.util.stream.Collectors;

public class BlockBuilder extends MapOnlyMapper<List<Entity>, Void> {

    private static final Logger LOGGER = Logger.getLogger(BlockBuilder.class.getName());

    private transient Closeable objectifySession;

    public BlockBuilder(ResourceId formId) {
        this.formId = formId;
    }

    private ResourceId formId;

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
    public void map(List<Entity> batch) {

        Hrd.ofy().transact(new VoidWork() {
            @Override
            public void vrun() {

                List<Key<FormRecordEntity>> recordKeys = batch.stream()
                        .map(e -> Key.<FormRecordEntity>create(e.getKey()))
                        .collect(Collectors.toList());

                Collection<FormRecordEntity> records = Hrd.ofy().load()
                        .keys(recordKeys)
                        .values()
                        .stream()
                        .filter(r -> !isAlreadyNumbered(r))
                        .collect(Collectors.toList());


                if(records.isEmpty()) {
                    LOGGER.warning("Batch has no unnumbered records");
                    return;
                }

                FormEntity rootEntity = Hrd.ofy().load().key(FormEntity.key(formId)).now();
                FormClass formSchema = Hrd.ofy().load().key(FormSchemaEntity.key(formId)).now().readFormClass();

                ColumnBlockUpdater blockUpdater = new ColumnBlockUpdater(rootEntity, formSchema, Hrd.ofy().getTransaction());

                for (FormRecordEntity record : records) {
                    updateBlocks(formSchema, rootEntity, record, blockUpdater);
                }

                LOGGER.info("Updating root entity " + Key.create(rootEntity).getRaw());

                List<Object> toSave = new ArrayList<>();
                toSave.add(rootEntity);
                toSave.addAll(records);
                toSave.addAll(blockUpdater.getUpdatedBlocks());

                Hrd.ofy().save().entities(toSave).now();

                getContext().getCounter("already-numbered").increment(batch.size() - records.size());
                getContext().getCounter("numbered").increment(records.size());

                LOGGER.info("Updated and indexed batch of " + records.size() + "/" + batch.size());
            }
        });
    }

    private boolean isAlreadyNumbered(FormRecordEntity record) {
        return record.getRecordNumber() > 0;
    }


    private void updateBlocks(FormClass formSchema, FormEntity rootEntity, FormRecordEntity record, ColumnBlockUpdater blockUpdater) {


        int newRecordCount = rootEntity.getNumberedRecordCount() + 1;
        int newRecordNumber = newRecordCount;

        LOGGER.info("Indexing record " + record + " as row " + newRecordNumber);

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
