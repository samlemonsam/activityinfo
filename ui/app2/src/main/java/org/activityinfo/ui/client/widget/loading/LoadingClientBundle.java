package org.activityinfo.ui.client.widget.loading;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface LoadingClientBundle extends ClientBundle {

    public static final LoadingClientBundle INSTANCE = GWT.create(LoadingClientBundle.class);

    @Source("large-loading.gif")
    ImageResource loadingIcon();
}
