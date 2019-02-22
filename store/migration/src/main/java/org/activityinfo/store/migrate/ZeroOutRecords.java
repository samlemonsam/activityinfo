package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ZeroOutRecords extends MapOnlyMapper<Entity,Void> {

    private static final Logger LOGGER = Logger.getLogger(ZeroOutRecords.class.getName());

    @Override
    public void map(Entity value) {

        Key recordKey = value.getKey();
        Key formKey = value.getParent();

        if(!formKey.getName().equals("a2142703274")){
            throw new RuntimeException("Ahhh wrong record" + recordKey);
        }

        Long number = (Long) value.getProperty("number");
        if(number == null || number == 0L) {
            return;
        }

        getContext().getCounter("non-zero").increment(1L);

        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction tx = datastoreService.beginTransaction();
        try {
            Entity toUpdate = datastoreService.get(tx, recordKey);
            toUpdate.setProperty("number", 0);
            datastoreService.put(tx, toUpdate);
            tx.commit();

        } catch (EntityNotFoundException ignored) {
            LOGGER.warning("Record " + recordKey + " has disappeared");
        } catch (Exception e) {
            try {
                LOGGER.log(Level.SEVERE, "Caught exception, rolling back tx", e);
                tx.rollback();
            } catch (Exception rollbackException) {
                LOGGER.log(Level.SEVERE, "Caught exception while rolling back", rollbackException);
            }
        }
    }
}
