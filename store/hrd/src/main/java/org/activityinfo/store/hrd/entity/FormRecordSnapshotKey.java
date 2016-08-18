package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Key for Form{formId} -> FormRecord{recordId} -> FormRecordSnapshot{version}
 */
public class FormRecordSnapshotKey implements TypedKey<FormRecordSnapshotEntity> {
    
    public static final String KIND = "Snapshot";
    
    private Key key;
    
    public FormRecordSnapshotKey(FormRecordKey recordKey, long version) {
        this.key = KeyFactory.createKey(recordKey.raw(), KIND, version);
    }
    
    @Override
    public FormRecordSnapshotEntity typeEntity(Entity entity) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Key raw() {
        return key;
    }
}
