package org.activityinfo.ui.client.input.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Styles and resources for form input.
 */
public interface InputResources extends ClientBundle {

    InputResources INSTANCE = GWT.create(InputResources.class);

    @Source("Input.gss")
    Style style();

    interface Style extends CssResource {

        String field();

        String fieldLabel();

        String form();

        String subform();
    }
}
