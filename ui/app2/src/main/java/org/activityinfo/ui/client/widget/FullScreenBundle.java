package org.activityinfo.ui.client.widget;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface FullScreenBundle extends ClientBundle {

    @Source("FullScreen.css")
    FullScreenStyle style();

    interface FullScreenStyle extends CssResource {

        String container();
    }

}
