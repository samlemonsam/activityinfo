package org.activityinfo.ui.client.frame;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LeftPanel implements IsWidget {

    interface MyUiBinder extends UiBinder<Widget, LeftPanel> {}
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private Widget root;

    public LeftPanel() {
        this.root = uiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return root;
    }
}
