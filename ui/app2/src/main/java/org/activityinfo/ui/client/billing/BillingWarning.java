package org.activityinfo.ui.client.billing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.activityinfo.model.account.AccountStatus;

import java.util.Date;

public class BillingWarning extends Composite {
    interface BillingWarningUiBinder extends UiBinder<HTMLPanel, BillingWarning> {
    }

    private static BillingWarningUiBinder ourUiBinder = GWT.create(BillingWarningUiBinder.class);


    @UiField
    DivElement warningText;
    @UiField
    Button closeButton;

    public BillingWarning(AccountStatus status) {
        initWidget(ourUiBinder.createAndBindUi(this));

        int secondsNow = (int) (new Date().getTime() / 1000);
        int secondsUntil = status.getExpirationTime() - secondsNow;
        int hours = (int) Math.round(secondsUntil / 3600d);
        String until;
        if (hours < 1) {
            until = "expired";
        } else if (hours <= 48) {
            until = "expiring in " + hours + " hours";
        } else {
            int days = (int) Math.round(hours / 24);
            if (days < 21) {
                until = "expiring in " + days + " days";
            } else {
                int weeks = (int) Math.round(days / 7d);
                until = "expiring in " + weeks + " weeks";
            }
        }

        if (!status.isTrial()) {
            warningText.setInnerHTML("<strong>Your free account is " + until + ".</strong> " +
                    "After this, you won't be able to access " + status.getDatabaseCount() + " databases you own. " +
                    "Please <a href=\"mailto:info@activityinfo.org\">contact us</a> to set up billing. " +
                    "Questions? Take a look at the <a href=\"http://help.activityinfo.org/\">FAQ</a>");
        }
    }

    @UiHandler("closeButton")
    public void closeButtonClick(ClickEvent event) {
        getElement().removeFromParent();
    }
}