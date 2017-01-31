package org.activityinfo.ui.client.widget;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.style.BaseStylesheet;

/**
 * ActivityInfo (will ultimately be) use a responsive, grid-based
 * layout that has a natural web flow. However, for some cases,
 * we really need a traditional application style layout that fits
 * to the size of the window and use traditional desktop-style layout.
 * <p/>
 * <p>The idea is that those components will live in a popup over the
 * rest of the document style layout.
 */
public class FullScreenOverlay {

    public static final int LEFT_MARGIN = 64;

    public static final int PADDING = 20;

    public static final FullScreenBundle BUNDLE = GWT.create(FullScreenBundle.class);

    private PopupPanel popupPanel;
    private AbsolutePanel container;
    private Widget widget;

    public FullScreenOverlay() {
        BaseStylesheet.INSTANCE.ensureInjected();
        BUNDLE.style().ensureInjected();

    }

    public void show(IsWidget widget) {
        this.widget = widget.asWidget();

        container = new AbsolutePanel();
        container.addStyleName(BaseStylesheet.CONTAINER_STYLE);
        container.addStyleName(BUNDLE.style().container());
        container.add(widget);
        sizeContainer();

        popupPanel = new PopupPanel(false);
        popupPanel.setPopupPosition(LEFT_MARGIN, 0);
        popupPanel.setWidget(container);
        popupPanel.show();

        Roles.getDialogRole().set(popupPanel.getElement());

        Window.addResizeHandler(new ResizeHandler() {

            Timer resizeTimer = new Timer() {
                @Override
                public void run() {
                    sizeContainer();
                }
            };

            @Override
            public void onResize(ResizeEvent event) {
                resizeTimer.cancel();
                resizeTimer.schedule(250);
            }
        });
    }

    private void sizeContainer() {
        container.setWidth((Window.getClientWidth() - LEFT_MARGIN) + "px");
        container.setHeight((Window.getClientHeight()) + "px");

        container.setWidgetPosition(widget, PADDING, PADDING);
        widget.setWidth((Window.getClientWidth() - LEFT_MARGIN - (PADDING * 2)) + "px");
        widget.setHeight((Window.getClientHeight() - (PADDING * 2)) + "px");
    }

    public void hide() {
        popupPanel.hide();
    }
}
