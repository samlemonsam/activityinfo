package org.activityinfo.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import org.activityinfo.ui.icons.Icons;
import org.activityinfo.ui.style.StyleBundle;
import org.activityinfo.ui.style.Styles;


public class App implements EntryPoint {
    @Override
    public void onModuleLoad() {

        Styles.ensureInjected();
        Icons.INSTANCE.ensureInjected();

        AppFrame appFrame = new AppFrame();
        RootPanel.get("root").add(appFrame);
    }
}
