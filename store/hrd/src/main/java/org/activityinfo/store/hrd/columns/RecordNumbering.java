package org.activityinfo.store.hrd.columns;

/**
 * Each form can maintain up to two record numbering schemes. This allows
 * one numbering scheme to be active while a second is updated to remove
 * deleted records.
 */
public enum RecordNumbering {
    BLUE,
    RED
}
