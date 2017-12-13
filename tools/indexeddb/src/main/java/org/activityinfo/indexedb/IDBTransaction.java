package org.activityinfo.indexedb;

/**
 * Interface to an Indexed DB Transaction
 */
public interface IDBTransaction {

    IDBObjectStore objectStore(String name);

    default  <T> T objectStore(ObjectStoreDefinition<T> definition) {
        return definition.wrap(objectStore(definition.getName()));
    }

}
