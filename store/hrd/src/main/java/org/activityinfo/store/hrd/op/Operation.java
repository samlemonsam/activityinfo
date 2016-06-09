package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.EntityNotFoundException;
import org.activityinfo.store.hrd.entity.Datastore;

public interface Operation {

    void execute(Datastore datastore) throws EntityNotFoundException;

}
