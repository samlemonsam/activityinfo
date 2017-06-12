package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;


public class SnapshotReindexer extends MapOnlyMapper<Entity, Void> {

    private transient DatastoreMutationPool pool;

    @Override
    public void beginSlice() {
        super.beginSlice();
        pool = DatastoreMutationPool.create();
    }

    @Override
    public void map(Entity entity) {

        if(entity.getProperty("version") == null) {
            entity.setIndexedProperty("version", entity.getKey().getId());
            pool.put(entity);
        }
    }

    @Override
    public void endSlice() {
        super.endSlice();
        pool.flush();
    }
}
