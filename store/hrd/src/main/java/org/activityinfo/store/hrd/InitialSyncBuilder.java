package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Stopwatch;
import com.googlecode.objectify.cmd.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static org.activityinfo.store.hrd.Hrd.ofy;

class InitialSyncBuilder {

    private static final Logger LOGGER = Logger.getLogger(InitialSyncBuilder.class.getName());

    private final FormClass formClass;
    private final Predicate<ResourceId> visibilityPredicate;

    private final List<FormRecord> records = new ArrayList<>();

    private Optional<String> cursor = Optional.empty();

    private SyncSizeEstimator sizeEstimator = new SyncSizeEstimator();

    InitialSyncBuilder(FormClass formClass, Predicate<ResourceId> visibilityPredicate) {
        this.formClass = formClass;
        this.visibilityPredicate = visibilityPredicate;
    }


    public void query(long toVersion, Optional<String> startAt) {
        LOGGER.info("Starting initial sync query...");
        Stopwatch stopwatch = Stopwatch.createStarted();


        Query<FormRecordEntity> query = ofy().load().type(FormRecordEntity.class)
                .ancestor(FormEntity.key(formClass))
                .chunk(500);

        if(startAt.isPresent()) {
            query = query.startAt(Cursor.fromWebSafeString(startAt.get()));
        }

        QueryResultIterator<FormRecordEntity> it = query.iterator();
        while(it.hasNext()) {
            FormRecordEntity record = it.next();
            if(visibilityPredicate.test(record.getRecordId())) {
                add(record);
                if(!sizeEstimator.timeAndSpaceRemaining()) {
                    stop(it.getCursor().toWebSafeString());
                    break;
                }
            }
        }

        LOGGER.info("Initial sync query complete in " + stopwatch.elapsed(TimeUnit.SECONDS) +
                " with estimate size: " + sizeEstimator.getEstimatedSizeInBytes() + " bytes");
    }


    /**
     * Adds this record to the {@link FormSyncSet}
     * @param record
     * @return {@code true} if there is time and space left to continue adding more records,
     * or if we should stop here.
     */
    public void add(FormRecordEntity record) {
        if(visibilityPredicate.test(record.getRecordId())) {
            records.add(record.toFormRecord(formClass));
            sizeEstimator.plus(record);
        }
    }

    public void stop(String cursor) {
        this.cursor = Optional.of(cursor);
    }

    public FormSyncSet build() {
        return FormSyncSet.initial(formClass.getId(), records, cursor);
    }

}
