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

    private final AccountStatus status;

    interface BillingWarningUiBinder extends UiBinder<HTMLPanel, BillingWarning> {
    }

    private static BillingWarningUiBinder ourUiBinder = GWT.create(BillingWarningUiBinder.class);


    @UiField
    DivElement warningText;

    @UiField
    Button closeButton;

    public BillingWarning(AccountStatus status) {
        this.status = status;
        initWidget(ourUiBinder.createAndBindUi(this));

        if(status.isTrial()) {
            warningText.setInnerHTML("<strong>Your free trial is " + status.expiringIn(new Date()) + ".</strong> " +
                    "After this, you won't be able to access " + status.getDatabaseCount() + " database(s) you own. " +
                    "Please <a href=\"mailto:info@activityinfo.org\">contact us</a> to set up billing. " +
                    "Questions? Take a look at the <a href=\"http://help.activityinfo.org/\">FAQ</a>");
        } else {
            warningText.setInnerHTML("<strong>Your account is " + status.expiringIn(new Date()) + ".</strong> " +
                    "To avoid interruption in service, <a href=\"mailto:info@activityinfo.org\">contact us</a>" +
                    " us to start the renewal process. ");
        }
    }

    @UiHandler("closeButton")
    public void closeButtonClick(ClickEvent event) {
        BillingSupervisor.snooze(status.snoozeDate(new Date()));
        getElement().removeFromParent();
    }

}