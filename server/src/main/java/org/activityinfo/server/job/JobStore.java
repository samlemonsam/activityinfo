package org.activityinfo.server.job;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * ObjectifyService wrapper which ensures entity classes are registered.
 */
public class JobStore {
    static {
        ObjectifyService.register(JobEntity.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static LoadResult<JobEntity> getUserJob(String websafeKey) {
        return ofy().load().key(Key.<JobEntity>create(websafeKey));
    }

    public static String getWebSafeKeyString(JobEntity job) {
        return Key.create(job).toWebSafeString();
    }
}
