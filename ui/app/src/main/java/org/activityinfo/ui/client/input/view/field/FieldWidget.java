package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.IsWidget;
import org.activityinfo.model.type.FieldValue;

/**
 * Interface to widgets that display field values and accept user input.
 */
public interface FieldWidget extends IsWidget {

    void init(FieldValue value);

    void setRelevant(boolean relevant);

}
