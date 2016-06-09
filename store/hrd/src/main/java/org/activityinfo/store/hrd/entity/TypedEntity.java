package org.activityinfo.store.hrd.entity;

import com.google.appengine.api.datastore.Entity;

/**
 * Typed wrapper around a datastore entity
 */
public interface TypedEntity {
    
    Entity raw();
}
