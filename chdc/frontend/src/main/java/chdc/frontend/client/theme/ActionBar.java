package chdc.frontend.client.theme;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;

public class ActionBar implements IsWidget {

    private FlowLayoutContainer outer;
    private FlowLayoutContainer inner;
    private FlowLayoutContainer shortcuts;

    public ActionBar() {
        shortcuts = new FlowLayoutContainer();
        shortcuts.setStyleName("actionbar__shortcuts");

        inner = new FlowLayoutContainer();
        inner.setStyleName("actionbar__inner");
        inner.add(shortcuts);

        outer = new FlowLayoutContainer();
        outer.setStyleName("actionbar");
        outer.add(inner);
    }

    @Override
    public Widget asWidget() {
        return outer;
    }

    public void addShortcut(IsWidget widget) {
        FlowLayoutContainer shortcutContainer = new FlowLayoutContainer();
        shortcutContainer.add(widget);

        shortcuts.add(shortcutContainer);
    }
}
