package org.activityinfo.service.store;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

/**
 * Contract for obtaining a {@link Cursor}
 * to iterate over a the Resources in a Collection.
 *
 * <p>Callers must explicitly specify which fields to retrieve by calling
 * {@code addField()}
 */
public interface CursorBuilder {


    void addResourceId(CursorObserver<ResourceId> observer);

    void addField(ExprNode node, CursorObserver<FieldValue> observer);

    void addField(FieldPath fieldPath, CursorObserver<FieldValue> observer);

    /**
     * Adds a field to the cursor
     * @param fieldId the id of the field to add to the ResourceId
     * @param observer an observer to receive the id of each {@code Reso}
     */
    void addField(ResourceId fieldId, CursorObserver<FieldValue> observer);


    /**
     *
     * Opens the cursor at the beginning of the {@code Collection}.
     *
     * @return an open Cursor
     */
    Cursor open();



}
