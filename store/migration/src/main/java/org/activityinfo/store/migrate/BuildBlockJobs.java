package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.tools.pipeline.ImmediateValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.google.common.base.Stopwatch;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.hrd.op.ColumnBlockUpdater;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BuildBlockJobs extends Job0<Void> {

    private static final Logger LOGGER = Logger.getLogger(BuildBlockJobs.class.getName());

    private static final int BATCH_SIZE = 10;

    private ResourceId formId;

    public BuildBlockJobs(String formId) {
        this.formId = ResourceId.valueOf(formId);
    }

    @Override
    public Value<Void> run() throws Exception {

        new MigrateSchema(formId).run();

        try(Closeable o = ObjectifyService.begin()) {

            for (int i = 0; i < 2; ++i) {

                Hrd.ofy().transact(new VoidWork() {
                    @Override
                    public void vrun() {
                        buildBatch();
                    }
                });
            }
        }

        return new ImmediateValue<>(null);
    }

    private void buildBatch() {


        LOGGER.info("Starting block update transaction...");

        Stopwatch stopwatch = Stopwatch.createStarted();

        FormEntity rootEntity = Hrd.ofy().load().key(FormEntity.key(formId)).now();
        FormClass formSchema = Hrd.ofy().load().key(FormSchemaEntity.key(formId)).now().readFormClass();

        List<FormRecordEntity> updatedRecords = new ArrayList<>();

        ColumnBlockUpdater blockUpdater = new ColumnBlockUpdater(formSchema, Hrd.ofy().getTransaction());

        // Loop through all the records, looking for ones that have not been assigned
        // a record number yet.

        QueryResultIterator<FormRecordEntity> it = Hrd.ofy()
                .load()
                .type(FormRecordEntity.class)
                .ancestor(rootEntity)
                .iterator();

        int updatedCount = 0;
        while (it.hasNext() && updatedCount < BATCH_SIZE) {
            FormRecordEntity record = it.next();
            if (record.getRecordNumber() == 0) {
                updateBlock(formSchema, rootEntity, record, blockUpdater);
                updatedRecords.add(record);
                updatedCount++;
            }
        }

        // Commit the changes
        if (!updatedRecords.isEmpty()) {
            List<Object> toSave = new ArrayList<>();

            LOGGER.info("Updating root entity " + Key.create(rootEntity).getRaw());

            toSave.add(rootEntity);
            toSave.addAll(updatedRecords);
            toSave.addAll(blockUpdater.getUpdatedBlocks());
            Hrd.ofy().save().entities(toSave).now();

            LOGGER.info(String.format("Updated batch of %s (%d entities) in %s",
                    updatedRecords.size(), toSave.size(), stopwatch));

        }
    }

    private void updateBlock(FormClass formSchema, FormEntity rootEntity, FormRecordEntity record, ColumnBlockUpdater blockUpdater) {
        int newRecordCount = rootEntity.getRecordCount() + 1;
        int newRecordNumber = newRecordCount;

        record.setRecordNumber(newRecordNumber);
        rootEntity.setRecordCount(newRecordCount);

        blockUpdater.updateId(newRecordNumber, record.getRecordId().asString());

        if(record.getParentRecordId() != null) {
            blockUpdater.updateParentId(newRecordNumber, record.getParentRecordId());
        }

        FormRecord formRecord = record.toFormRecord(formSchema);
        FormInstance typedRecord = FormInstance.toFormInstance(formSchema, formRecord);

        blockUpdater.updateFields(newRecordNumber, typedRecord.getFieldValueMap());
    }


}
