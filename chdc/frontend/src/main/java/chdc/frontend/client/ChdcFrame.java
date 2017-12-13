package chdc.frontend.client;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class ChdcFrame implements IsWidget {


    private final VerticalLayoutContainer container;
    private final SimpleContainer inner;

    public ChdcFrame() {


//        HtmlLayoutContainer sidebar = new HtmlLayoutContainer(
//                TEMPLATES.getSidebar(
//                        RESOURCES.getStyle()));
//
//
        inner = new SimpleContainer();

        container = new VerticalLayoutContainer();
        container.add(new Banner(), new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        container.add(inner, new VerticalLayoutContainer.VerticalLayoutData(1, 1));

    }

    public AcceptsOneWidget getDisplayWidget() {
        return new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget w) {
                inner.setWidget(w);
                container.forceLayout();
            }
        };
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
