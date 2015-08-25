package org.activityinfo.ui.client.frame;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class DummyWidget implements IsWidget {

    interface MyUiBinder extends UiBinder<Widget, DummyWidget> {}
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private Widget root;
    @UiField
    SpanElement localeSpan;

    public DummyWidget() {
        root = uiBinder.createAndBindUi(this);
        localeSpan.setInnerText(LocaleInfo.getCurrentLocale().getLocaleName());
    }

    @Override
    public Widget asWidget() {
        return root;
    }
}
