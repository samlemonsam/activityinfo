package org.activityinfo.ui.client.store;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Signals that a Form, its schema, or records has changed.
 */
public class FormChangeEvent extends GwtEvent<FormChangeEventHandler> {
    public static Type<FormChangeEventHandler> TYPE = new Type<FormChangeEventHandler>();

    public Type<FormChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    private final FormChange predicate;

    public FormChangeEvent(FormChange predicate) {
        this.predicate = predicate;
    }

    public FormChange getPredicate() {
        return predicate;
    }

    protected void dispatch(FormChangeEventHandler handler) {
        handler.onFormChange(this);
    }
}
