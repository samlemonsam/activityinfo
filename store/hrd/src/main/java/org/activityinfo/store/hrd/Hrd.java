package org.activityinfo.store.hrd;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Work;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;

/**
 * Gateway to ObjectifyService that ensures entity classes are registered
 */
public class Hrd {
    static {
        ObjectifyService.register(FormEntity.class);
        ObjectifyService.register(FormRecordEntity.class);
        ObjectifyService.register(FormRecordSnapshotEntity.class);
        ObjectifyService.register(FormSchemaEntity.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static <T> T run(Work<T> work) {
        return ObjectifyService.run(work);
    }
}
