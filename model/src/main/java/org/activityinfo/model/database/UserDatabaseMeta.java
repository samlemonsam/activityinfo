package org.activityinfo.model.database;

import java.util.List;

/**
 * Describes a single user's view of database, including the folders, forms,
 * and locks visible to the user, as well as their own permissions within this database.
 */
public class UserDatabaseMeta {
    private int databaseId;
    private int userId;
    private String name;
    private boolean owner;

    private List<DatabaseFolder> folders;
    private List<DatabaseForm> forms;
    private List<GrantModel> grants;
    private List<DatabaseLock> locks;





}
