package org.activityinfo.store.mysql;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created by yuriyz on 8/17/2016.
 */
public class DatabasesFolder {

    public static final String ROOT_ID = "databases";

    private final QueryExecutor executor;

    public DatabasesFolder(QueryExecutor executor) {
        this.executor = executor;
    }

    public CatalogEntry getRootEntry() {
        return new CatalogEntry(ROOT_ID, I18N.CONSTANTS.databases(), CatalogEntryType.FOLDER);
    }

    public List<CatalogEntry> getChildren(String parentId) throws SQLException {
        if(parentId.equals(ROOT_ID)) {
            return queryDatabases();
        }
        return Collections.emptyList();
    }

    private List<CatalogEntry> queryDatabases() {
        return Collections.emptyList();
    }
}
