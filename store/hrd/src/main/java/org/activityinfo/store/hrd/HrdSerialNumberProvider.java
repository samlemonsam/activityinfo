package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.SerialNumberProvider;


public class HrdSerialNumberProvider implements SerialNumberProvider {

    public static final String KIND = "SerialNumber";
    public static final String PROPERTY = "Serial";

    private DatastoreService datastore;

    public HrdSerialNumberProvider() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    public int next(ResourceId formId, ResourceId fieldId) {

        Key key = KeyFactory.createKey(KIND,
                formId.asString() + "$$$" + fieldId.asString());

        Transaction tx = datastore.beginTransaction();

        Entity entity;
        int number;
        try {
            entity = datastore.get(tx, key);
            number = ((Number)entity.getProperty(PROPERTY)).intValue();
            number = number + 1;

        } catch (EntityNotFoundException e) {
            entity = new Entity(key);
            number = 1;
        }

        entity.setProperty(PROPERTY, number);

        datastore.put(tx, entity);
        tx.commit();

        return number;
    }
}
