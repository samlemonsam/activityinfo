package org.activityinfo.ui.client.billing;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class BillingErrors {

    public static void freeTrialExpired() {
        MessageBox.alert(SafeHtmlUtils.fromString("Trial Expired"),
                SafeHtmlUtils.fromTrustedString(
                        "Oh no! Your free trial has expired. " +
                "To continue, <a href=\"mailto:info@activityinfo.org\">contact us</a> to " +
                                " set up billing."), null);
    }
}
