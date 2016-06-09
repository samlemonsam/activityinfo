package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


public interface TypedKey<E> {
    
    E typeEntity(Entity entity);
    
    Key raw();
}
