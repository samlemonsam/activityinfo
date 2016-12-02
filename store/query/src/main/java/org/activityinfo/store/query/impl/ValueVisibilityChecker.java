package org.activityinfo.store.query.impl;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

/**
 * Created by yuriyz on 8/31/2016.
 */
public interface ValueVisibilityChecker {

    ValueVisibilityChecker NULL = new ValueVisibilityChecker() {
        @Override
        public void assertVisible(FieldType fieldType, FieldValue value, int userId) {
        }
    };

    void assertVisible(FieldType fieldType, FieldValue value, int userId);
}
