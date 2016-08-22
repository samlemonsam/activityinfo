package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Work;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class QuerySubRecords implements Work<List<FormRecord>> {

    private FormClass formClass;
    private ResourceId parentRecordId;

    public QuerySubRecords(FormClass formClass, ResourceId parentRecordId) {
        this.formClass = formClass;
        this.parentRecordId = parentRecordId;
    }

    @Override
    public List<FormRecord> run() {
        QueryResultIterable<FormRecordEntity> query = ofy().load()
                .type(FormRecordEntity.class)
                .ancestor(FormEntity.key(formClass))
                .filter("parentRecordId", this.parentRecordId.asString())
                .iterable();

        List<FormRecord> records = Lists.newArrayList();
        for (FormRecordEntity entity : query) {
            records.add(entity.toFormRecord(formClass));

        }
        return records;
    }
}
