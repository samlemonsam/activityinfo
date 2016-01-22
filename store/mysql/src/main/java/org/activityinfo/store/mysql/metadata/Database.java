package org.activityinfo.store.mysql.metadata;

/**
 * Stores metadata on Userdatabase and the current cache versions of Activities
 */
public class Database {
    
    private int id;
    private String name;


    /**
     * The current version of the schema
     */
    private long schemaVersion;
    
}
