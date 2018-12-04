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

public class PaymentWarning extends Composite {

    private final AccountStatus status;

    interface PaymentWarningUiBinder extends UiBinder<HTMLPanel, PaymentWarning> {
    }

    private static PaymentWarningUiBinder ourUiBinder = GWT.create(PaymentWarningUiBinder.class);

    @UiField
    DivElement warningText;

    @UiField
    Button closeButton;

    public PaymentWarning(AccountStatus status) {
        this.status = status;
        initWidget(ourUiBinder.createAndBindUi(this));
        if(status.isPaymentOverdue()) {
            warningText.setInnerHTML("<strong>Your account is overdue.</strong> " +
                    "To avoid interruption in service, <a href=\"mailto:info@activityinfo.org\">contact us</a> " +
                    "to discuss the payment status. ");
        } else {
            warningText.setInnerHTML("<strong>Your account has a payment " + status.paymentExpectedIn(new Date()) + ".</strong> " +
                    "To avoid interruption in service, <a href=\"mailto:info@activityinfo.org\">contact us</a> " +
                    "to discuss the payment status. ");
        }
    }

    @UiHandler("closeButton")
    public void closeButtonClick(ClickEvent event) {
        if (!status.isPaymentOverdue()) {
            BillingSupervisor.snooze(status, BillingSupervisor.PAYMENT_SNOOZE_KEY_PREFIX);
        }
        getElement().removeFromParent();
    }

}