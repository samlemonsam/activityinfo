package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.CursorObserver;

public class FieldObserver {
    
    private final String name;
    private final FieldConverter<?> converter;
    private final CursorObserver<FieldValue> observer;

    public FieldObserver(String name, FieldConverter<?> converter, CursorObserver<FieldValue> observer) {
        this.name = name;
        this.converter = converter;
        this.observer = observer;
    }

    public void onNext(EmbeddedEntity entity) {
        Object value = entity.getProperty(name);
        if(value == null) {
            observer.onNext(null);
        } else {
            observer.onNext(converter.toFieldValue(value));
        }
    }
}
