package org.activityinfo.store.query.shared;


/**
 * Defines the level of filters that are applied to a query.
 */
public enum FilterLevel {

    /**
     * No filter applied to the raw results from FormStorage
     */
    NONE,

    /**
     * Sub-records of deleted parent records are hidden
     */
    BASE,

    /**
     * Form- and record-level permissions are applied in addition
     * to the "BASE" filters.
     */
    PERMISSIONS
}
