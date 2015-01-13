package org.activityinfo.store.mysql.mapping;


public class PrimaryKeyMapping {
    private final String columnName;
    private final char domain;

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
