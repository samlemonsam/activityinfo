package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface to widgets that display field values and accept user input.
 */
public interface FieldWidget extends IsWidget {

    void setRelevant(boolean relevant);
}
