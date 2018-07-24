package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.KeyFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

@Entity(name = "Block")
public class BlockEntity {

    public static com.google.appengine.api.datastore.Key key(Key<FormEntity> formKey, String fieldId, int blockIndex) {
        com.google.appengine.api.datastore.Key fieldKey = KeyFactory.createKey(formKey.getRaw(),"FormField", fieldId);
        return KeyFactory.createKey(fieldKey, "Block", blockIndex);
    }

}