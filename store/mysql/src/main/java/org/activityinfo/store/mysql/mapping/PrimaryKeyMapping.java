package org.activityinfo.store.mysql.mapping;


/**
 * Maps a collections' {@code resourceId} to a table's primary key
 */
public class PrimaryKeyMapping {
    private final char domain;
    private final String columnName;

    public PrimaryKeyMapping(char domain, String columnName) {
        this.columnName = columnName;
        this.domain = domain;
    }

    public String getColumnName() {
        return columnName;
    }

    public char getDomain() {
        return domain;
    }
}
