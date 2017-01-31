package org.activityinfo.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;

public class ProgressBar implements IsWidget {


    @UiField DivElement barElement;
    @UiField SpanElement screenReaderText;

    private final HTMLPanel rootElement;

    interface ProgressBarUiBinder extends UiBinder<HTMLPanel, ProgressBar> {
    }

    private static ProgressBarUiBinder ourUiBinder = GWT.create(ProgressBarUiBinder.class);

    public ProgressBar() {
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    /**
     *
     * @param percentComplete a percentage between 0 and 100
     */
    public void setValue(int percentComplete) {
        assert percentComplete >= 0 && percentComplete <= 100;
        barElement.getStyle().setWidth(percentComplete, Style.Unit.PCT);
        barElement.setAttribute("aria-valuenow", Integer.toString(percentComplete));
        screenReaderText.setInnerText(I18N.MESSAGES.percentComplete(percentComplete));
    }

    public void setVisible(boolean visible) {
        rootElement.setVisible(visible);
    }

    public boolean isVisible() {
        return rootElement.isVisible();
    }

}