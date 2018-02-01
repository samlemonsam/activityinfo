package org.activityinfo.store;

public interface TransactionalStorageProvider {

    void begin();

    void commit();

    void rollback();


}
