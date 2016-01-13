package org.activityinfo.ui.client.frame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;


public class MainPanel implements IsWidget {

    interface MyUiBinder extends UiBinder<Widget, MainPanel> {}
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private Widget root;

    public MainPanel() {
        this.root = uiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return root;
    }
}
