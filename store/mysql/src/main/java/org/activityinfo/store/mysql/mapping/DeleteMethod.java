package org.activityinfo.store.mysql.mapping;

/**
 * Describes how row deletions are handled
 */
public enum DeleteMethod {

    /**
     * Rows are marked as deleted by a dateDeleted field
     */
    SOFT_BY_DATE,

    /**
     * Rows are marked as deleted by a deleted boolean field
     */
    SOFT_BY_BOOLEAN,

    
}
