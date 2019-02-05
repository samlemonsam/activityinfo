package org.activityinfo.model.resource;

public enum TransactionMode {

    /**
     * Validates a transaction against the form's current schema and fails if the update is not
     * consistent with the schema.
     */
    STRICT,

    /**
     * Executes a transaction that was initiated by the user offline.
     *
     * <p>Allowances are made for changes that may have been committed to the server between the moment
     * that the offline user last synced and the moment that the transaction was composed offline.</p>
     *
     * <p>For example, the following errors should be ignored:</p>
     * <ul>
     *     <li>Updates to a form that no longer exists</li>
     *     <li>Deletion of a record that has already been deleted</li>
     *     <li>Newly-required fields</li>
     *     <li>Enumerated values referencing items that have been deleted</li>
     * </ul>
     */
    OFFLINE
}
