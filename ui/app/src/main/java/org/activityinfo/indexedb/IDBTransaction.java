package org.activityinfo.indexedb;

/**
 * Interface to an Indexed DB Transaction
 */
public interface IDBTransaction {

    IDBObjectStore objectStore(String name);

    <T> T objectStore(ObjectStoreDefinition<T> definition);

}
