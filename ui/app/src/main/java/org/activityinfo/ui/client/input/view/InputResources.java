package org.activityinfo.ui.client.input.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.sencha.gxt.themebuilder.base.client.config.ThemeDetails;

/**
 * Styles and resources for form input.
 */
public interface InputResources extends ClientBundle {

    InputResources INSTANCE = GWT.create(InputResources.class);

    @Source("Input.gss")
    Style style();

    ThemeDetails theme();

    interface Style extends CssResource {

        String field();

        String fieldLabel();

        String form();

        String subform();

        String periodToolBar();
    }
}
