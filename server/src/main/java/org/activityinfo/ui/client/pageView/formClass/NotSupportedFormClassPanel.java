package org.activityinfo.ui.client.pageView.formClass;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;

/**
 * Created by yuriyz on 8/19/2016.
 */
public class NotSupportedFormClassPanel implements IsWidget {

    private final HTML html;

    public NotSupportedFormClassPanel() {
        this.html = new HTML("<h4>" + I18N.CONSTANTS.formIsNotSupported2_12() + "</h2>");
    }

    @Override
    public Widget asWidget() {
        return html;
    }
}
