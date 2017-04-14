package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.*;
import com.google.common.base.Strings;
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
    public int next(ResourceId formId, ResourceId fieldId, String prefix) {

        StringBuilder keyString = new StringBuilder();
        keyString.append(formId.asString());
        keyString.append('\0');
        keyString.append(fieldId.asString());

        if(!Strings.isNullOrEmpty(prefix)) {
            keyString.append('\0');
            keyString.append(prefix);
        }

        Key key = KeyFactory.createKey(KIND, keyString.toString());

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
