package org.activityinfo.indexedb;


public interface IDBDatabase {

    String READONLY = "readonly";

    String READWRITE = "readwrite";

    void transaction(String[] objectStores, String mode, IDBTransactionCallback callback);

    void close();

}
