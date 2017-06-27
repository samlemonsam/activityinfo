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

    private final FormChange change;

    public FormChangeEvent(FormChange change) {
        this.change = change;
    }

    public FormChange getChange() {
        return change;
    }

    protected void dispatch(FormChangeEventHandler handler) {
        handler.onFormChange(this);
    }
}
