package org.activityinfo.ui.client.component.importDialog;

import com.google.web.bindery.event.shared.Event;

public class PageChangedEvent extends Event<PageChangedEventHandler> {

    public static Type<PageChangedEventHandler> TYPE = new Type<PageChangedEventHandler>();

    private boolean valid;
    private String statusMessage;

    public PageChangedEvent(boolean valid, String statusMessage) {
        super();
        this.valid = valid;
        this.statusMessage = statusMessage;
    }

    @Override
    public Type<PageChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PageChangedEventHandler handler) {
        handler.onPageChanged(this);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
