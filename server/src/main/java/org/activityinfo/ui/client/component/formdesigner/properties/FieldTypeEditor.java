package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.user.client.ui.IsWidget;
import org.activityinfo.model.form.FormField;

/**
 * Interface to UI for
 */
public interface FieldTypeEditor extends IsWidget {

    /**
     * Called when a new field is selected.
     */
    void fieldSelected(FormField field);
}
