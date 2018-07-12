package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.SubFormPatch;


public class PeriodObserver implements CursorObserver<Entity> {

    private static final String PROPERTY_NAME = SubFormPatch.PERIOD_FIELD_ID.asString();
    private final PeriodType periodType;
    private final CursorObserver<FieldValue> observer;

    public PeriodObserver(FormClass formClass, CursorObserver<FieldValue> observer) {
        periodType = formClass.getSubFormKind().getPeriodType();
        this.observer = observer;
    }

    @Override
    public void onNext(Entity value) {
        EmbeddedEntity fieldValues = (EmbeddedEntity) value.getProperty("fieldValues");
        Object property = fieldValues.getProperty(PROPERTY_NAME);
        if(property instanceof String) {
            observer.onNext(periodType.parseString((String) property));
        } else {
            Key recordKey = value.getKey();
            Key formKey = recordKey.getParent();

            RecordRef ref = new RecordRef(ResourceId.valueOf(formKey.getName()), ResourceId.valueOf(recordKey.getName()));
            observer.onNext(periodType.fromSubFormKey(ref));
        }
    }

    @Override
    public void done() {
        observer.done();
    }
}
