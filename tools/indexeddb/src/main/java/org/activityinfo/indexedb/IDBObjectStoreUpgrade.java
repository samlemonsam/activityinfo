package org.activityinfo.indexedb;

public interface IDBObjectStoreUpgrade {

    void createIndex(String indexName, String keyPath, IndexOptions indexOptions);

}
