package org.activityinfo.ui.client.widget;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;

/**
 * Simple close button that uses the unicode x symbol
 */
public class CloseButton extends Button {

    public CloseButton() {
        super(SafeHtmlUtils.fromSafeConstant("&times;"));
        setStyleName("close");
        getButtonElement().setAttribute("aria-hidden", "true");
    }
}
