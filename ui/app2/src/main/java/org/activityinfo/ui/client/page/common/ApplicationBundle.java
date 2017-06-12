package org.activityinfo.ui.client.page.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ApplicationBundle extends ClientBundle {

    public static final ApplicationBundle INSTANCE = GWT.create(ApplicationBundle.class);

    @Source("Application.css")
    Styles styles();

    public interface Styles extends CssResource {

        String over();

        @ClassName("loading-indicator")
        String loadingIndicator();

        String gallery();

        String indicatorTable();

        String indicatorHeading();

        String comments();

        String appTitle();

        String unmapped();

        @ClassName("cell-hover")
        String cellHover();

        String groupName();

        String mapped();

        String details();

        String indicatorQuantity();

        @ClassName("loading-placeholder")
        String loadingPlaceholder();

        String indicatorGroupHeading();

        String indicatorGroupChild();
    }
}
