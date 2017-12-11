package chdc.frontend.client.theme;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

public class MainDisplay implements AcceptsOneWidget {

    private final RootPanel rootPanel;

    private IsWidget currentMainWidget = null;

    public MainDisplay(RootPanel rootPanel) {
        this.rootPanel = rootPanel;
    }

    @Override
    public void setWidget(IsWidget w) {
        if(currentMainWidget != w) {
            if(currentMainWidget != null) {
                rootPanel.remove(currentMainWidget);
            }
            if(w != null) {
                rootPanel.add(w);
            }
            currentMainWidget = w;
        }
    }
}
