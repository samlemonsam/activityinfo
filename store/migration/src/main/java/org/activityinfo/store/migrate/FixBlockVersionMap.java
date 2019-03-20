package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.google.common.primitives.Ints;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.columns.TombstoneBlock;
import org.activityinfo.store.hrd.entity.ColumnDescriptor;
import org.activityinfo.store.hrd.entity.FormEntity;

import java.util.logging.Logger;

public class FixBlockVersionMap extends MapOnlyMapper<Entity, Void> {

    private static final Logger LOGGER = Logger.getLogger(FixBlockVersionMap.class.getName());

    @Override
    public void map(Entity formEntity) {
        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {

                FormEntity form = Hrd.ofy().load().fromEntity(formEntity);
                if(!form.isColumnStorageActive()) {
                    return;
                }
                if(form.getId().startsWith("E")) {
                    return;
                }


                Hrd.ofy().transact(new VoidWork() {
                    @Override
                    public void vrun() {
                        fixVersionMap(form.getResourceId());
                    }
                });
            }
        });
    }

    private void fixVersionMap(ResourceId formId) {

        FormEntity form = Hrd.ofy().load().key(FormEntity.key(formId)).safe();
        long newVersion = form.getVersion() + 1;

        LOGGER.info("Fixing " + form.getId() + ", current version = " + form.getVersion());


        form.setVersion(newVersion);

        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Block", Key.create(form).getRaw());

        PreparedQuery preparedQuery = datastoreService.prepare(Hrd.ofy().getTransaction(), query);

        boolean dirty = false;

        for (Entity block : preparedQuery.asIterable()) {
            int blockIndex = Ints.checkedCast(block.getKey().getId() - 1L);
            String columnId = block.getKey().getParent().getName();

            if(columnId.equals("$ID") || columnId.equals("@parent") || columnId.equals(TombstoneBlock.COLUMN_NAME)) {
                continue;
            }

            ColumnDescriptor descriptor = form.getBlockColumns().get(columnId);
            if(descriptor == null) {
                LOGGER.severe(columnId + "@" + blockIndex + " is missing descriptor!");
            } else {
                long existingVersion = descriptor.getBlockVersion(blockIndex);
                if(existingVersion <= 0) {
                    LOGGER.info(columnId + "@" + blockIndex + " from " + existingVersion + " => " + newVersion);
                    descriptor.setBlockVersion(blockIndex, newVersion);
                    dirty = true;
                } else {
                    LOGGER.info(columnId + "@" + blockIndex + " already at " + existingVersion);
                }
            }
        }
        if(dirty) {
            LOGGER.warning("Form " + formId + " UPDATED");
            getContext().getCounter("fixed").increment(1L);
            Hrd.ofy().save().entity(form).now();
        }
    }
}
