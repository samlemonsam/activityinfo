package org.activityinfo.store.hrd.op;

import org.activityinfo.store.hrd.entity.Datastore;


public interface QueryOperation<T> {
    
    
    T execute(Datastore datastore);
    
    
}
