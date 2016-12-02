package org.activityinfo.store.query.impl.eval;

/**
 * The type of join made
 */
public enum JoinType {

    /**
     * Join based on a reference field (on the left) containing the ids of form submissions (on the right)
     */
    REFERENCE,

    /**
     * Join based on a master form submission ids (on the left) and the parent ids of submissions (on the right)
     */
    SUBFORM
    
}
