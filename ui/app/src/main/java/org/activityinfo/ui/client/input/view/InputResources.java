package org.activityinfo.ui.client.input.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Created by alex on 16-2-17.
 */
public interface InputResources extends ClientBundle {

    InputResources INSTANCE = GWT.create(InputResources.class);

    @Source("Input.gss")
    Style style();

    interface Style extends CssResource {

        String field();

        String fieldLabel();

        String form();
    }
}
