package org.activityinfo.ui.client;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class AppFrame implements IsWidget {

    interface MyUiBinder extends UiBinder<Widget, AppFrame> {}
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private Widget root;

    public AppFrame() {
        this.root = uiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return root;
    }
}
